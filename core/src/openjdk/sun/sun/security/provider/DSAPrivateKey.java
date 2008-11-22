/*
 * Copyright 1996-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.security.provider;

import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.AlgorithmParameters;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.interfaces.DSAParams;

import sun.security.x509.AlgIdDSA;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.Debug;
import sun.security.util.DerValue;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;

/**
 * A PKCS#8 private key for the Digital Signature Algorithm.
 *
 * @author Benjamin Renaud
 *
 *
 * @see DSAPublicKey
 * @see AlgIdDSA
 * @see DSA
 */

public final class DSAPrivateKey extends PKCS8Key 
implements java.security.interfaces.DSAPrivateKey, Serializable {

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -3244453684193605938L;

    /* the private key */
    private BigInteger x;

    /*
     * Keep this constructor for backwards compatibility with JDK1.1.
     */
    public DSAPrivateKey() {
    }

    /**
     * Make a DSA private key out of a private key and three parameters.
     */
    public DSAPrivateKey(BigInteger x, BigInteger p, 
			 BigInteger q, BigInteger g) 
    throws InvalidKeyException {
	this.x = x;
	algid = new AlgIdDSA(p, q, g);

	try {
	    key = new DerValue(DerValue.tag_Integer, 
			       x.toByteArray()).toByteArray();
	    encode();
	} catch (IOException e) {
	    InvalidKeyException ike = new InvalidKeyException(
                "could not DER encode x: " + e.getMessage());
	    ike.initCause(e);
	    throw ike;
	}
    }

    /**
     * Make a DSA private key from its DER encoding (PKCS #8).
     */
    public DSAPrivateKey(byte[] encoded) throws InvalidKeyException {
	clearOldKey();
	decode(encoded);
    }

    /**
     * Returns the DSA parameters associated with this key, or null if the
     * parameters could not be parsed.
     */
    public DSAParams getParams() {
	try {
	    if (algid instanceof DSAParams) {
		return (DSAParams)algid;
	    } else {
		DSAParameterSpec paramSpec;
		AlgorithmParameters algParams = algid.getParameters();
		if (algParams == null) {
		    return null;
		}
                paramSpec = algParams.getParameterSpec(DSAParameterSpec.class);
		return (DSAParams)paramSpec;
	    }
	} catch (InvalidParameterSpecException e) {
	    return null;
	}
    }

    /**
     * Get the raw private key, x, without the parameters.
     *
     * @see getParameters
     */
    public BigInteger getX() {
	return x;
    }

    private void clearOldKey() {
	int i;
	if (this.encodedKey != null) {
	    for (i = 0; i < this.encodedKey.length; i++) {
		this.encodedKey[i] = (byte)0x00;
	    }
	}
	if (this.key != null) {
	    for (i = 0; i < this.key.length; i++) {
		this.key[i] = (byte)0x00;
	    }
	}
    }

    public String toString() {
	return "Sun DSA Private Key \nparameters:" + algid + "\nx: " +
	    Debug.toHexString(x) + "\n";
    }

    protected void parseKeyBits() throws InvalidKeyException {
	try {
	    DerInputStream in = new DerInputStream(key);
	    x = in.getBigInteger();
	} catch (IOException e) {
	    InvalidKeyException ike = new InvalidKeyException(e.getMessage());
	    ike.initCause(e);
	    throw ike;
	}
    }
}
