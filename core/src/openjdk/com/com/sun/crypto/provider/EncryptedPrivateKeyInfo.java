/*
 * Copyright 1998-2007 Sun Microsystems, Inc.  All Rights Reserved.
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
import sun.security.x509.AlgorithmId;
import sun.security.util.*;

/**
 * This class implements the <code>EncryptedPrivateKeyInfo</code> type,
 * which is defined in PKCS #8 as follows:
 *
 * <pre>
 * EncryptedPrivateKeyInfo ::=  SEQUENCE {
 *     encryptionAlgorithm   AlgorithmIdentifier,
 *     encryptedData   OCTET STRING }
 * </pre>
 *
 * @author Jan Luehe
 */
final class EncryptedPrivateKeyInfo {

    // the "encryptionAlgorithm" field
    private AlgorithmId algid;

    // the "encryptedData" field
    private byte[] encryptedData;

    // the ASN.1 encoded contents of this class
    private byte[] encoded;

    /**
     * Constructs (i.e., parses) an <code>EncryptedPrivateKeyInfo</code> from
     * its encoding.
     */
    EncryptedPrivateKeyInfo(byte[] encoded) throws IOException {
        DerValue val = new DerValue(encoded);

        DerValue[] seq = new DerValue[2];

        seq[0] = val.data.getDerValue();
        seq[1] = val.data.getDerValue();

        if (val.data.available() != 0) {
            throw new IOException("overrun, bytes = " + val.data.available());
        }

        this.algid = AlgorithmId.parse(seq[0]);
        if (seq[0].data.available() != 0) {
            throw new IOException("encryptionAlgorithm field overrun");
        }

        this.encryptedData = seq[1].getOctetString();
        if (seq[1].data.available() != 0)
            throw new IOException("encryptedData field overrun");

        this.encoded = (byte[])encoded.clone();
    }

    /**
     * Constructs an <code>EncryptedPrivateKeyInfo</code> from the
     * encryption algorithm and the encrypted data.
     */
    EncryptedPrivateKeyInfo(AlgorithmId algid, byte[] encryptedData) {
        this.algid = algid;
        this.encryptedData = (byte[])encryptedData.clone();
        this.encoded = null; // lazy generation of encoding
    }

    /**
     * Returns the encryption algorithm.
     */
    AlgorithmId getAlgorithm() {
        return this.algid;
    }

    /**
     * Returns the encrypted data.
     */
    byte[] getEncryptedData() {
        return (byte[])this.encryptedData.clone();
    }

    /**
     * Returns the ASN.1 encoding of this class.
     */
    byte[] getEncoded()
        throws IOException
    {
        if (this.encoded != null) return (byte[])this.encoded.clone();

        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();

        // encode encryption algorithm
        algid.encode(tmp);

        // encode encrypted data
        tmp.putOctetString(encryptedData);

        // wrap everything into a SEQUENCE
        out.write(DerValue.tag_Sequence, tmp);
        this.encoded = out.toByteArray();

        return (byte[])this.encoded.clone();
    }
}
