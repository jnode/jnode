/*
 * Copyright 2005-2007 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class implements a key factory for PBE keys derived using
 * PBKDF2 with HmacSHA1 psuedo random function(PRF) as defined in
 * PKCS#5 v2.0.
 *
 * @author Valerie Peng
 *
 */
public final class PBKDF2HmacSHA1Factory extends SecretKeyFactorySpi {

    /**
     * Verify the SunJCE provider in the constructor.
     *
     * @exception SecurityException if fails to verify
     * its own integrity
     */
    public PBKDF2HmacSHA1Factory() {
        if (!SunJCE.verifySelfIntegrity(this.getClass())) {
            throw new SecurityException("The SunJCE provider may have " +
                                        "been tampered.");
        }
    }

    /**
     * Generates a <code>SecretKey</code> object from the provided key
     * specification (key material).
     *
     * @param keySpec the specification (key material) of the secret key
     *
     * @return the secret key
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a public key.
     */
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (!(keySpec instanceof PBEKeySpec)) {
            throw new InvalidKeySpecException("Invalid key spec");
        }
        PBEKeySpec ks = (PBEKeySpec) keySpec;
        return new PBKDF2KeyImpl(ks, "HmacSHA1");
    }

    /**
     * Returns a specification (key material) of the given key
     * in the requested format.
     *
     * @param key the key
     *
     * @param keySpec the requested format in which the key material shall be
     * returned
     *
     * @return the underlying key specification (key material) in the
     * requested format
     *
     * @exception InvalidKeySpecException if the requested key
     * specification is inappropriate for the given key, or the
     * given key cannot be processed (e.g., the given key has an
     * unrecognized algorithm or format).
     */
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpecCl)
        throws InvalidKeySpecException {
        if (key instanceof javax.crypto.interfaces.PBEKey) {
            // Check if requested key spec is amongst the valid ones
            if ((keySpecCl != null)
                && PBEKeySpec.class.isAssignableFrom(keySpecCl)) {
                javax.crypto.interfaces.PBEKey pKey =
                    (javax.crypto.interfaces.PBEKey) key;
                return new PBEKeySpec
                    (pKey.getPassword(), pKey.getSalt(),
                     pKey.getIterationCount(), pKey.getEncoded().length*8);
            } else {
                throw new InvalidKeySpecException("Invalid key spec");
            }
        } else {
            throw new InvalidKeySpecException("Invalid key " +
                                               "format/algorithm");
        }
    }

    /**
     * Translates a <code>SecretKey</code> object, whose provider may be
     * unknown or potentially untrusted, into a corresponding
     * <code>SecretKey</code> object of this key factory.
     *
     * @param key the key whose provider is unknown or untrusted
     *
     * @return the translated key
     *
     * @exception InvalidKeyException if the given key cannot be processed by
     * this key factory.
     */
    protected SecretKey engineTranslateKey(SecretKey key)
        throws InvalidKeyException {
        if ((key != null) &&
            (key.getAlgorithm().equalsIgnoreCase("PBKDF2WithHmacSHA1")) &&
            (key.getFormat().equalsIgnoreCase("RAW"))) {

            // Check if key originates from this factory
            if (key instanceof com.sun.crypto.provider.PBKDF2KeyImpl) {
                return key;
            }
            // Check if key implements the PBEKey
            if (key instanceof javax.crypto.interfaces.PBEKey) {
                javax.crypto.interfaces.PBEKey pKey =
                    (javax.crypto.interfaces.PBEKey) key;
                try {
                    PBEKeySpec spec =
                        new PBEKeySpec(pKey.getPassword(),
                                       pKey.getSalt(),
                                       pKey.getIterationCount(),
                                       pKey.getEncoded().length*8);
                    return new PBKDF2KeyImpl(spec, "HmacSHA1");
                } catch (InvalidKeySpecException re) {
                    InvalidKeyException ike = new InvalidKeyException
                        ("Invalid key component(s)");
                    ike.initCause(re);
                    throw ike;
                }
            }
        }
        throw new InvalidKeyException("Invalid key format/algorithm");
    }
}
