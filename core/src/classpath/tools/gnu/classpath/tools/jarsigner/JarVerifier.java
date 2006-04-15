/* JarVerifier.java -- The verification handler of the gjarsigner tool
   Copyright (C) 2006 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA. */


package gnu.classpath.tools.jarsigner;

import gnu.java.security.OID;
import gnu.java.security.Registry;
import gnu.java.security.pkcs.PKCS7SignedData;
import gnu.java.security.pkcs.SignerInfo;
import gnu.java.security.sig.ISignature;
import gnu.java.security.sig.ISignatureCodec;
import gnu.java.security.sig.dss.DSSSignature;
import gnu.java.security.sig.dss.DSSSignatureX509Codec;
import gnu.java.security.sig.rsa.RSAPKCS1V1_5Signature;
import gnu.java.security.sig.rsa.RSAPKCS1V1_5SignatureX509Codec;
import gnu.java.security.util.Util;
import gnu.java.util.jar.JarUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipException;

/**
 * The JAR verification handler of the <code>gjarsigner</code> tool.
 */
public class JarVerifier
{
  private static final Logger log = Logger.getLogger(JarVerifier.class.getName());
  /** The owner tool of this handler. */
  private Main main;
  private HashUtils util = new HashUtils();
  /** The JAR file to verify. */
  private JarFile jarFile;
  /** Map of jar entry names to their hash. */
  private Map entryHashes = new HashMap();

  JarVerifier(Main main)
  {
    super();

    this.main = main;
  }

  void start() throws Exception
  {
    log.entering("JarVerifier", "start");

    String jarFileName = main.getJarFileName();
    jarFile = new JarFile(jarFileName);

    // 1. find all signature (.SF) files
    List sfFiles = new ArrayList();
    for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
      {
        JarEntry je = (JarEntry) e.nextElement();
        String jeName = je.getName();
        if (! (jeName.startsWith(JarUtils.META_INF)
            && jeName.endsWith(JarUtils.SF_SUFFIX)))
          continue;

        // only interested in .SF files in, and not deeper than, META-INF
        String[] jeNameParts = jeName.split("/");
        if (jeNameParts.length != 2)
          continue;

        String sfName = jeNameParts[1];
        String sigFileName = sfName.substring(0, sfName.length() - 3);
        sfFiles.add(sigFileName);
      }

    // 2. verify each one
    if (sfFiles.isEmpty())
      System.out.println("jar is not signed.--no signature files found.");
    else
      {
        int limit = sfFiles.size();
        int count = 0;
        for (Iterator it = sfFiles.iterator(); it.hasNext(); )
          {
            String alias = (String) it.next();
            if (verifySF(alias))
              if (verifySFEntries(alias))
                count++;
          }

        if (count == 0)
          System.out.println("jar verification failed.");
        else if (count != limit)
          System.out.println("jar partially verified --" + count + " of "
                             + limit + " signers.");
        else
          System.out.println("jar verified --" + limit + " signer(s).");
      }

    log.exiting("JarVerifier", "start");
  }

  /**
   * @param sigFileName the name of the signature file; i.e. the name to use for
   *          both the .SF and .DSA files.
   * @return <code>true</code> if the designated file-name (usually a key-store
   * <i>alias</i> name) has been successfully checked as the signer of the
   * corresponding <code>.SF</code> file. Returns <code>false</code> otherwise.
   * @throws IOException
   * @throws ZipException
   * @throws CertificateException
   * @throws CRLException
   */
  private boolean verifySF(String sigFileName) throws CRLException,
      CertificateException, ZipException, IOException
  {
    log.entering("JarVerifier", "verifySF");
    log.finest("About to verify signature of " + sigFileName + "...");

    // 1. find the corresponding .DSA file for this .SF file
    JarEntry dsaEntry = jarFile.getJarEntry(JarUtils.META_INF + sigFileName
                                            + JarUtils.DSA_SUFFIX);
    if (dsaEntry == null)
      throw new SecurityException("Signature Block missing for " + sigFileName);

    // 2. read the .DSA file contents as a PKCS7 SignedData
    InputStream in = jarFile.getInputStream(dsaEntry);
    PKCS7SignedData pkcs7SignedData = new PKCS7SignedData(in);

    // 4. get the encrypted digest octet string from the first SignerInfo
    //    this octet string is the digital signature of the .SF file contents
    Set signerInfos = pkcs7SignedData.getSignerInfos();
    if (signerInfos == null || signerInfos.isEmpty())
      throw new SecurityException("At least one SignerInfo element MUST be "
                                  + "present in a Signature Block (.DSA file)");
    SignerInfo signerInfo = (SignerInfo) signerInfos.iterator().next();
    byte[] encryptedDigest = signerInfo.getEncryptedDigest();
    if (encryptedDigest == null)
      throw new SecurityException("Missing EncryptedDigest in Signature Block "
                                  + "(.DSA file) first SignerInfo element");
    log.finest("\n" + Util.dumpString(encryptedDigest, "--- signedSFBytes "));

    // 5. get the signer public key
    Certificate cert = pkcs7SignedData.getCertificates()[0];
    PublicKey verifierKey = cert.getPublicKey();
    log.finest("--- verifier public key = " + verifierKey);

    // 6. verify the signature file signature
    OID digestEncryptionAlgorithmOID = signerInfo.getDigestEncryptionAlgorithmId();
    ISignature signatureAlgorithm;
    ISignatureCodec signatureCodec;
    if (digestEncryptionAlgorithmOID.equals(Main.DSA_SIGNATURE_OID))
      {
        signatureAlgorithm = new DSSSignature();
        signatureCodec = new DSSSignatureX509Codec();
      }
    else
      {
        signatureAlgorithm = new RSAPKCS1V1_5Signature(Registry.MD5_HASH);
        signatureCodec = new RSAPKCS1V1_5SignatureX509Codec();
      }

    Map signatureAttributes = new HashMap();
    signatureAttributes.put(ISignature.VERIFIER_KEY, verifierKey);
    signatureAlgorithm.setupVerify(signatureAttributes);

    Object herSignature = signatureCodec.decodeSignature(encryptedDigest);

    // 7. verify the signature file contents
    JarEntry sfEntry = jarFile.getJarEntry(JarUtils.META_INF + sigFileName
                                           + JarUtils.SF_SUFFIX);
    in = jarFile.getInputStream(sfEntry);
    byte[] buffer = new byte[2048];
    int n;
    while ((n = in.read(buffer)) != -1)
      if (n > 0)
        signatureAlgorithm.update(buffer, 0, n);

    boolean result = signatureAlgorithm.verify(herSignature);
    log.info("Signature block [" + sigFileName + "] is "
             + (result ? "" : "NOT ") + "OK");

    log.exiting("JarVerifier", "verifySF", new Boolean(result));
    return result;
  }

  /**
   * This method is called after at least one signer (usually a key-store
   * <code>alias</code> name) was found to be trusted; i.e. his/her signature
   * block in the corresponding <code>.DSA</code> file was successfully
   * verified using his/her public key.
   * <p>
   * This method, uses the contents of the corresponding <code>.SF</code> file
   * to compute and verify the hashes of the manifest entries in the JAR file.
   * 
   * @param alias the name of the signature file; i.e. the name to use for both
   *          the .SF and .DSA files.
   * @return <code>true</code> if all the entries in the corresponding
   *         <code>.SF</code> file have the same hash values as their
   *         alter-ego in the <i>manifest</i> file of the JAR file inquestion.
   * @throws IOException if an I/O related exception occurs during the process.
   */
  private boolean verifySFEntries(String alias) throws IOException
  {
    log.entering("JarVerifier", "verifySFEntries");

    // 1. read the signature file
    JarEntry jarEntry = jarFile.getJarEntry(JarUtils.META_INF + alias
                                            + JarUtils.SF_SUFFIX);
    InputStream in = jarFile.getInputStream(jarEntry);
    Attributes attr = new Attributes();
    Map entries = new HashMap();
    JarUtils.readSFManifest(attr, entries, in);

    // 2. The .SF file by default includes a header containing a hash of the
    // entire manifest file. When the header is present, then the verification
    // can check to see whether or not the hash in the header indeed matches
    // the hash of the manifest file.
    boolean result = false;
    String hash = attr.getValue(Main.DIGEST_MANIFEST_ATTR);
    if (hash != null)
      result = verifyManifest(hash);

    // A verification is still considered successful if none of the files that
    // were in the JAR file when the signature was generated have been changed
    // since then, which is the case if the hashes in the non-header sections
    // of the .SF file equal the hashes of the corresponding sections in the
    // manifest file.
    //
    // 3. Read each file in the JAR file that has an entry in the .SF file.
    // While reading, compute the file's digest, and then compare the result
    // with the digest for this file in the manifest section. The digests
    // should be the same, or verification fails.
    if (! result)
      for (Iterator it = entries.keySet().iterator(); it.hasNext();)
        {
          Entry me = (Entry) it.next();
          String name = (String) me.getKey();
          attr = (Attributes) me.getValue();
          hash = attr.getValue(Main.DIGEST_ATTR);
          result = verifySFEntry(name, hash);
          if (! result)
            break;
        }

    log.exiting("JarVerifier", "verifySFEntries", new Boolean(result));
    return result;
  }

  /**
   * @param hash Base-64 encoded form of the manifest's digest.
   * @return <code>true</code> if our computation of the manifest's hash
   *         matches the given value; <code>false</code> otherwise.
   * @throws IOException if unable to acquire the JAR's manifest entry.
   */
  private boolean verifyManifest(String hash) throws IOException
  {
    return verifySFEntry(JarFile.MANIFEST_NAME, hash);
  }

  /**
   * @param name the name of a JAR entry to verify.
   * @param hash Base-64 encoded form of the designated entry's digest.
   * @return <code>true</code> if our computation of the JAR entry's hash
   *         matches the given value; <code>false</code> otherwise.
   * @throws IOException if an exception occurs while returning the entry's
   *           input stream.
   */
  private boolean verifySFEntry(String name, String hash) throws IOException
  {
    String expectedValue = getEntryHash(JarFile.MANIFEST_NAME);
    boolean result = expectedValue.equalsIgnoreCase(hash);
    log.finest("Is " + name + " OK? " + result);
    return result;
  }

  private String getEntryHash(String entryName) throws IOException
  {
    String result = (String) entryHashes.get(entryName);
    if (result == null)
      {
        JarEntry manifest = jarFile.getJarEntry(entryName);
        InputStream in = jarFile.getInputStream(manifest);
        result = util.hashStream(in);
        entryHashes.put(entryName, result);
      }

    return result;
  }
}
