/* JarSigner.java -- The signing handler of the gjarsigner tool
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

import gnu.classpath.SystemProperties;
import gnu.java.util.jar.JarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

/**
 * The JAR signing handler of the <code>gjarsigner</code> tool.
 */
public class JarSigner
{
  private static final Logger log = Logger.getLogger(JarSigner.class.getName());
  /** The owner tool of this handler. */
  private Main main;

  JarSigner(Main main)
  {
    super();

    this.main = main;
  }

  void start() throws Exception
  {
    log.entering("JarSigner", "start");

    JarFile jarFile = new JarFile(main.getJarFileName());
    SFHelper sfHelper = new SFHelper(jarFile);

    sfHelper.startSigning();

    // 1. compute the digests
    for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
      {
        JarEntry je = (JarEntry) e.nextElement();
        String jeName = je.getName();
        if (jeName.equals(JarFile.MANIFEST_NAME)
            || jeName.endsWith(File.separator))
          continue;

        sfHelper.updateEntry(je);
        if (main.isVerbose())
          System.out.println("  signing: " + jeName);
      }

    sfHelper.finishSigning(main.isSectionsOnly());
    if (main.isVerbose())
      System.out.println(" updating: " + JarFile.MANIFEST_NAME);

    // 2. write jar entries and manifest
    File signedJarFile = File.createTempFile("gcp-", ".jar");
    FileOutputStream fos = new FileOutputStream(signedJarFile);
    JarOutputStream outSignedJarFile = new JarOutputStream(fos,
                                                           sfHelper.getManifest());
    for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
      {
        JarEntry je = (JarEntry) e.nextElement();
        String jeName = je.getName();
        if (jeName.equals(JarFile.MANIFEST_NAME)
            || jeName.endsWith(File.separator))
          continue;

        log.finest("Processing " + jeName);
        JarEntry newEntry = new JarEntry(jeName);
        newEntry.setTime(je.getTime());
        outSignedJarFile.putNextEntry(newEntry);
        InputStream jeis = jarFile.getInputStream(je);
        copyFromTo(jeis, outSignedJarFile);
      }

    // 3. create the .SF file
    String signaturesFileName = main.getSigFileName();
    String sfFileName = JarUtils.META_INF + signaturesFileName
                        + JarUtils.SF_SUFFIX;
    log.finest("Processing " + sfFileName);
    JarEntry sfEntry = new JarEntry(sfFileName);
    sfEntry.setTime(System.currentTimeMillis());
    outSignedJarFile.putNextEntry(sfEntry);
    sfHelper.writeSF(outSignedJarFile);
    log.info("Created .SF file");
    if (main.isVerbose())
      System.out.println("   adding: " + sfFileName);

    // 4. create the .DSA file
    String dsaFileName = JarUtils.META_INF + signaturesFileName
                         + JarUtils.DSA_SUFFIX;
    log.finest("Processing " + dsaFileName);
    JarEntry dsaEntry = new JarEntry(dsaFileName);
    dsaEntry.setTime(System.currentTimeMillis());
    outSignedJarFile.putNextEntry(dsaEntry);
    sfHelper.writeDSA(outSignedJarFile,
                      main.getSignerPrivateKey(),
                      main.getSignerCertificateChain(),
                      main.isInternalSF());
    log.info("Created .DSA file");
    if (main.isVerbose())
      System.out.println("   adding: " + dsaFileName);

    // cleanup
    outSignedJarFile.close();
    fos.close();
    signedJarFile.renameTo(new File(main.getSignedJarFileName()));
    log.info("Renamed signed JAR file");
    if (main.isVerbose())
      System.out.println(SystemProperties.getProperty("line.separator")
                         + "jar signed.");

    log.exiting("JarSigner", "start");
  }

  private void copyFromTo(InputStream in, JarOutputStream out)
    throws IOException
  {
    byte[] buffer = new byte[8192];
    int n;
    while ((n = in.read(buffer)) != -1)
      if (n > 0)
        out.write(buffer, 0, n);
  }
}
