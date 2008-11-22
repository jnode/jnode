/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.security.provider.certpath;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.x509.*;
import sun.security.util.*;

/**
 * This class corresponds to the CertId field in OCSP Request 
 * and the OCSP Response. The ASN.1 definition for CertID is defined
 * in RFC 2560 as:
 * <pre>
 *
 * CertID          ::=     SEQUENCE {
 *      hashAlgorithm       AlgorithmIdentifier,
 *      issuerNameHash      OCTET STRING, -- Hash of Issuer's DN
 *      issuerKeyHash       OCTET STRING, -- Hash of Issuers public key
 *      serialNumber        CertificateSerialNumber 
 *	}
 * 
 * </pre>
 *
 * @author	Ram Marti
 */

public class CertId {

    private static final boolean debug = false;
    private AlgorithmId hashAlgId;
    private byte[] issuerNameHash;
    private byte[] issuerKeyHash;
    private SerialNumber certSerialNumber;
    private int myhash = -1; // hashcode for this CertId

    /**
     * Creates a CertId. The hash algorithm used is SHA-1. 
     */
    public CertId(X509CertImpl issuerCert, SerialNumber serialNumber) 
	throws Exception {

	// compute issuerNameHash
	MessageDigest md = MessageDigest.getInstance("SHA1");
	hashAlgId = AlgorithmId.get("SHA1");
	md.update(issuerCert.getSubjectX500Principal().getEncoded());
	issuerNameHash = md.digest();

	// compute issuerKeyHash (remove the tag and length)
	byte[] pubKey = issuerCert.getPublicKey().getEncoded();
	DerValue val = new DerValue(pubKey);
	DerValue[] seq = new DerValue[2];	
	seq[0] = val.data.getDerValue(); // AlgorithmID
	seq[1] = val.data.getDerValue(); // Key
	byte[] keyBytes = seq[1].getBitString();
	md.update(keyBytes);
	issuerKeyHash = md.digest();
	certSerialNumber = serialNumber;

	if (debug) {
	    HexDumpEncoder encoder = new HexDumpEncoder();
	    System.out.println("Issuer Certificate is " + issuerCert);
	    System.out.println("issuerNameHash is " +
		encoder.encode(issuerNameHash)); 
	    System.out.println("issuerKeyHash is " +
		encoder.encode(issuerKeyHash)); 
	}
    }

    /**
     * Creates a CertId from its ASN.1 DER encoding.
     */
    public CertId(DerInputStream derIn) throws IOException {

	hashAlgId = AlgorithmId.parse(derIn.getDerValue());
	issuerNameHash = derIn.getOctetString();
	issuerKeyHash = derIn.getOctetString();
	certSerialNumber = new SerialNumber(derIn);
    }
	
    /**
     * Return the hash algorithm identifier.
     */
    public AlgorithmId getHashAlgorithm() {
	return hashAlgId;
    }

    /**
     * Return the hash value for the issuer name.
     */
    public byte[] getIssuerNameHash() {
	return issuerNameHash;
    }

    /**
     * Return the hash value for the issuer key.
     */
    public byte[] getIssuerKeyHash() {
	return issuerKeyHash;
    }

    /**
     * Return the serial number.
     */
    public BigInteger getSerialNumber() {
	return certSerialNumber.getNumber();
    }

    /**
     * Encode the CertId using ASN.1 DER.
     * The hash algorithm used is SHA-1.
     */
    public void encode(DerOutputStream out) throws IOException {

	DerOutputStream tmp = new DerOutputStream();
	hashAlgId.encode(tmp);
	tmp.putOctetString(issuerNameHash);
	tmp.putOctetString(issuerKeyHash);
	certSerialNumber.encode(tmp);
	out.write(DerValue.tag_Sequence, tmp);

	if (debug) {
	    HexDumpEncoder encoder = new HexDumpEncoder();
	    System.out.println("Encoded certId is " +
		encoder.encode(out.toByteArray()));
	}
    }

   /**
     * Returns a hashcode value for this CertId.
     *
     * @return the hashcode value.
     */
    public int hashCode() {
        if (myhash == -1) {
            myhash = hashAlgId.hashCode();
	    for (int i = 0; i < issuerNameHash.length; i++) {
		myhash += issuerNameHash[i] * i;
	    }
	    for (int i = 0; i < issuerKeyHash.length; i++) {
		myhash += issuerKeyHash[i] * i;
	    }
	    myhash += certSerialNumber.getNumber().hashCode();
        }
        return myhash;
    }

    /**
     * Compares this CertId for equality with the specified
     * object. Two CertId objects are considered equal if their hash algorithms,
     * their issuer name and issuer key hash values and their serial numbers 
     * are equal.
     *
     * @param other the object to test for equality with this object.
     * @return true if the objects are considered equal, false otherwise.
     */
    public boolean equals(Object other) {

        if (this == other) {
            return true;
	}
        if (other == null || (!(other instanceof CertId))) {
            return false;
	}

	CertId that = (CertId) other;
	if (hashAlgId.equals(that.getHashAlgorithm()) &&
	    Arrays.equals(issuerNameHash, that.getIssuerNameHash()) &&
	    Arrays.equals(issuerKeyHash, that.getIssuerKeyHash()) &&
	    certSerialNumber.getNumber().equals(that.getSerialNumber())) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Create a string representation of the CertId.
     */
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("CertId \n");
	sb.append("Algorithm: " + hashAlgId.toString() +"\n");
	sb.append("issuerNameHash \n");
	HexDumpEncoder encoder = new HexDumpEncoder();
	sb.append(encoder.encode(issuerNameHash));
	sb.append("\nissuerKeyHash: \n");
	sb.append(encoder.encode(issuerKeyHash));
	sb.append("\n" +  certSerialNumber.toString());
	return sb.toString();
    }
}
