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

import java.math.BigInteger;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHGenParameterSpec;

import sun.security.provider.ParameterCache;

/**
 * This class represents the key pair generator for Diffie-Hellman key pairs.
 *
 * <p>This key pair generator may be initialized in two different ways:
 *
 * <ul>
 * <li>By providing the size in bits of the prime modulus -
 * This will be used to create a prime modulus and base generator, which will
 * then be used to create the Diffie-Hellman key pair. The default size of the
 * prime modulus is 1024 bits.
 * <li>By providing a prime modulus and base generator
 * </ul>
 *
 * @author Jan Luehe
 *
 *
 * @see java.security.KeyPairGenerator
 */
public final class DHKeyPairGenerator extends KeyPairGeneratorSpi {

    // parameters to use or null if not specified
    private DHParameterSpec params;

    // The size in bits of the prime modulus
    private int pSize;

    // The size in bits of the random exponent (private value)
    private int lSize;

    // The source of randomness
    private SecureRandom random;

    public DHKeyPairGenerator() {
        super();
        initialize(1024, null);
    }

    /**
     * Initializes this key pair generator for a certain keysize and source of
     * randomness.
     * The keysize is specified as the size in bits of the prime modulus.
     *
     * @param keysize the keysize (size of prime modulus) in bits
     * @param random the source of randomness
     */
    public void initialize(int keysize, SecureRandom random) {
        if ((keysize < 512) || (keysize > 1024) || (keysize % 64 != 0)) {
            throw new InvalidParameterException("Keysize must be multiple "
                                                + "of 64, and can only range "
                                                + "from 512 to 1024 "
                                                + "(inclusive)");
        }
        this.pSize = keysize;
        this.lSize = 0;
        this.random = random;
        this.params = null;
    }

    /**
     * Initializes this key pair generator for the specified parameter
     * set and source of randomness.
     *
     * <p>The given parameter set contains the prime modulus, the base
     * generator, and optionally the requested size in bits of the random
     * exponent (private value).
     *
     * @param params the parameter set used to generate the key pair
     * @param random the source of randomness
     *
     * @exception InvalidAlgorithmParameterException if the given parameters
     * are inappropriate for this key pair generator
     */
    public void initialize(AlgorithmParameterSpec algParams,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (!(algParams instanceof DHParameterSpec)){
            throw new InvalidAlgorithmParameterException
                ("Inappropriate parameter type");
        }

        params = (DHParameterSpec)algParams;
        pSize = params.getP().bitLength();
        if ((pSize < 512) || (pSize > 1024) ||
            (pSize % 64 != 0)) {
            throw new InvalidAlgorithmParameterException
                ("Prime size must be multiple of 64, and can only range "
                 + "from 512 to 1024 (inclusive)");
        }

        // exponent size is optional, could be 0
        lSize = params.getL();

        // Require exponentSize < primeSize
        if ((lSize != 0) && (lSize > pSize)) {
            throw new InvalidAlgorithmParameterException
                ("Exponent size must not be larger than modulus size");
        }
        this.random = random;
    }

    /**
     * Generates a key pair.
     *
     * @return the new key pair
     */
    public KeyPair generateKeyPair() {
        if (random == null) {
            random = SunJCE.RANDOM;
        }

        if (params == null) {
            try {
                params = ParameterCache.getDHParameterSpec(pSize, random);
            } catch (GeneralSecurityException e) {
                // should never happen
                throw new ProviderException(e);
            }
        }

        BigInteger p = params.getP();
        BigInteger g = params.getG();

        if (lSize <= 0) {
            // use an exponent size of (pSize / 2) but at least 384 bits
            lSize = Math.max(384, pSize >> 1);
            // if lSize is larger than pSize, limit by pSize
            lSize = Math.min(lSize, pSize);
        }

        BigInteger x;
        BigInteger pMinus2 = p.subtract(BigInteger.valueOf(2));

        //
        // Handbook of Applied Cryptography:  Menezes, et.al.
        // Repeat if the following does not hold:
        //     1 <= x <= p-2
        //
        do {
            // generate random x up to 2^lSize bits long
            x = new BigInteger(lSize, random);
        } while ((x.compareTo(BigInteger.ONE) < 0) ||
            ((x.compareTo(pMinus2) > 0)));

        // calculate public value y
        BigInteger y = g.modPow(x, p);

        DHPublicKey pubKey = new DHPublicKey(y, p, g, lSize);
        DHPrivateKey privKey = new DHPrivateKey(x, p, g, lSize);
        return new KeyPair(pubKey, privKey);
    }
}
