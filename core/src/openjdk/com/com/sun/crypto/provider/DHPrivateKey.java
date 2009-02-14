/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.crypto.provider;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyRep;
import java.security.PrivateKey;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import javax.crypto.*;
import javax.crypto.spec.DHParameterSpec;
import sun.security.util.*;

/**
 * A private key in PKCS#8 format for the Diffie-Hellman key agreement
 * algorithm.
 *
 * @author Jan Luehe
 *
 *
 * @see DHPublicKey
 * @see java.security.KeyAgreement
 */
final class DHPrivateKey implements PrivateKey,
javax.crypto.interfaces.DHPrivateKey, Serializable {

    static final long serialVersionUID = 7565477590005668886L;

    // only supported version of PKCS#8 PrivateKeyInfo
    private static final BigInteger PKCS8_VERSION = BigInteger.ZERO;

    // the private key
    private BigInteger x;

    // the key bytes, without the algorithm information
    private byte[] key;

    // the encoded key
    private byte[] encodedKey;

    // the prime modulus
    private BigInteger p;

    // the base generator
    private BigInteger g;

    // the private-value length
    private int l;

    private int DH_data[] = { 1, 2, 840, 113549, 1, 3, 1 };

    /**
     * Make a DH private key out of a private value <code>x</code>, a prime
     * modulus <code>p</code>, and a base generator <code>g</code>.
     *
     * @param x the private value
     * @param p the prime modulus
     * @param g the base generator
     *
     * @exception ProviderException if the key cannot be encoded
     */
    DHPrivateKey(BigInteger x, BigInteger p, BigInteger g)
        throws InvalidKeyException {
        this(x, p, g, 0);
    }

    /**
     * Make a DH private key out of a private value <code>x</code>, a prime
     * modulus <code>p</code>, a base generator <code>g</code>, and a
     * private-value length <code>l</code>.
     *
     * @param x the private value
     * @param p the prime modulus
     * @param g the base generator
     * @param l the private-value length
     *
     * @exception InvalidKeyException if the key cannot be encoded
     */
    DHPrivateKey(BigInteger x, BigInteger p, BigInteger g, int l) {
        this.x = x;
        this.p = p;
        this.g = g;
        this.l = l;
        try {
            this.key = new DerValue(DerValue.tag_Integer,
                                    this.x.toByteArray()).toByteArray();
            this.encodedKey = getEncoded();
        } catch (IOException e) {
            throw new ProviderException("Cannot produce ASN.1 encoding", e);
        }
    }

    /**
     * Make a DH private key from its DER encoding (PKCS #8).
     *
     * @param encodedKey the encoded key
     *
     * @exception InvalidKeyException if the encoded key does not represent
     * a Diffie-Hellman private key
     */
    DHPrivateKey(byte[] encodedKey) throws InvalidKeyException {
        InputStream inStream = new ByteArrayInputStream(encodedKey);
        try {
            DerValue val = new DerValue(inStream);
            if (val.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException ("Key not a SEQUENCE");
            }

            //
            // version
            //
            BigInteger parsedVersion = val.data.getBigInteger();
            if (!parsedVersion.equals(PKCS8_VERSION)) {
                throw new IOException("version mismatch: (supported: " +
                                      PKCS8_VERSION + ", parsed: " +
                                      parsedVersion);
            }

            //
            // privateKeyAlgorithm
            //
            DerValue algid = val.data.getDerValue();
            if (algid.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException("AlgId is not a SEQUENCE");
            }
            DerInputStream derInStream = algid.toDerInputStream();
            ObjectIdentifier oid = derInStream.getOID();
            if (oid == null) {
                throw new InvalidKeyException("Null OID");
            }
            if (derInStream.available() == 0) {
                throw new InvalidKeyException("Parameters missing");
            }
            // parse the parameters
            DerValue params = derInStream.getDerValue();
            if (params.tag == DerValue.tag_Null) {
                throw new InvalidKeyException("Null parameters");
            }
            if (params.tag != DerValue.tag_Sequence) {
                throw new InvalidKeyException("Parameters not a SEQUENCE");
            }
            params.data.reset();
            this.p = params.data.getBigInteger();
            this.g = params.data.getBigInteger();
            // Private-value length is OPTIONAL
            if (params.data.available() != 0) {
                this.l = params.data.getInteger();
            }
            if (params.data.available() != 0) {
                throw new InvalidKeyException("Extra parameter data");
            }

            //
            // privateKey
            //
            this.key = val.data.getOctetString();
            parseKeyBits();

            // ignore OPTIONAL attributes

            this.encodedKey = (byte[])encodedKey.clone();

        } catch (NumberFormatException e) {
            InvalidKeyException ike = new InvalidKeyException(
                "Private-value length too big");
            ike.initCause(e);
            throw ike;
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException("Error parsing key encoding: " + e.getMessage());
            ike.initCause(e);
            throw ike;
        }
    }

    /**
     * Returns the encoding format of this key: "PKCS#8"
     */
    public String getFormat() {
        return "PKCS#8";
    }

    /**
     * Returns the name of the algorithm associated with this key: "DH"
     */
    public String getAlgorithm() {
        return "DH";
    }

    /**
     * Get the encoding of the key.
     */
    public synchronized byte[] getEncoded() {
        if (this.encodedKey == null) {
            try {
                DerOutputStream tmp = new DerOutputStream();

                //
                // version
                //
                tmp.putInteger(PKCS8_VERSION);

                //
                // privateKeyAlgorithm
                //
                DerOutputStream algid = new DerOutputStream();

                // store OID
                algid.putOID(new ObjectIdentifier(DH_data));
                // encode parameters
                DerOutputStream params = new DerOutputStream();
                params.putInteger(this.p);
                params.putInteger(this.g);
                if (this.l != 0)
                    params.putInteger(this.l);
                // wrap parameters into SEQUENCE
                DerValue paramSequence = new DerValue(DerValue.tag_Sequence,
                                                      params.toByteArray());
                // store parameter SEQUENCE in algid
                algid.putDerValue(paramSequence);
                // wrap algid into SEQUENCE
                tmp.write(DerValue.tag_Sequence, algid);

                // privateKey
                tmp.putOctetString(this.key);

                // make it a SEQUENCE
                DerOutputStream derKey = new DerOutputStream();
                derKey.write(DerValue.tag_Sequence, tmp);
                this.encodedKey = derKey.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }
        return (byte[])this.encodedKey.clone();
    }

    /**
     * Returns the private value, <code>x</code>.
     *
     * @return the private value, <code>x</code>
     */
    public BigInteger getX() {
        return this.x;
    }

    /**
     * Returns the key parameters.
     *
     * @return the key parameters
     */
    public DHParameterSpec getParams() {
        if (this.l != 0)
            return new DHParameterSpec(this.p, this.g, this.l);
        else
            return new DHParameterSpec(this.p, this.g);
    }

    public String toString() {
        String LINE_SEP = System.getProperty("line.separator");

        StringBuffer strbuf
            = new StringBuffer("SunJCE Diffie-Hellman Private Key:"
                               + LINE_SEP + "x:" + LINE_SEP
                               + Debug.toHexString(this.x)
                               + LINE_SEP + "p:" + LINE_SEP
                               + Debug.toHexString(this.p)
                               + LINE_SEP + "g:" + LINE_SEP
                               + Debug.toHexString(this.g));
        if (this.l != 0)
            strbuf.append(LINE_SEP + "l:" + LINE_SEP + "    " + this.l);
        return strbuf.toString();
    }

    private void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(this.key);
            this.x = in.getBigInteger();
        } catch (IOException e) {
            InvalidKeyException ike = new InvalidKeyException("Error parsing key encoding: " + e.getMessage());
            ike.initCause(e);
            throw ike;
        }
    }

    /**
     * Calculates a hash code value for the object.
     * Objects that are equal will also have the same hashcode.
     */
    public int hashCode() {
        int retval = 0;
        byte[] enc = getEncoded();

        for (int i = 1; i < enc.length; i++) {
            retval += enc[i] * i;
        }
        return(retval);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof PrivateKey))
            return false;

        byte[] thisEncoded = this.getEncoded();
        byte[] thatEncoded = ((PrivateKey)obj).getEncoded();

        return java.util.Arrays.equals(thisEncoded, thatEncoded);
    }

    /**
     * Replace the DH private key to be serialized.
     *
     * @return the standard KeyRep object to be serialized
     *
     * @throws java.io.ObjectStreamException if a new object representing
     * this DH private key could not be created
     */
    private Object writeReplace() throws java.io.ObjectStreamException {
        return new KeyRep(KeyRep.Type.PRIVATE,
                        getAlgorithm(),
                        getFormat(),
                        getEncoded());
    }
}
