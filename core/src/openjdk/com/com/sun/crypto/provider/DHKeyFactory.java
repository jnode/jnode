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

import java.util.*;
import java.lang.*;
import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyFactorySpi;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHParameterSpec;

/**
 * This class implements the Diffie-Hellman key factory of the Sun provider.
 *
 * @author Jan Luehe
 *
 */
public final class DHKeyFactory extends KeyFactorySpi {

    /**
     * Verify the SunJCE provider in the constructor.
     *
     * @exception SecurityException if fails to verify
     * its own integrity
     */
    public DHKeyFactory() {
        if (!SunJCE.verifySelfIntegrity(this.getClass())) {
            throw new SecurityException("The SunJCE provider may have " +
                                        "been tampered.");
        }
    }

    /**
     * Generates a public key object from the provided key specification
     * (key material).
     *
     * @param keySpec the specification (key material) of the public key
     *
     * @return the public key
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a public key.
     */
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        try {
            if (keySpec instanceof DHPublicKeySpec) {
                DHPublicKeySpec dhPubKeySpec = (DHPublicKeySpec)keySpec;
                return new DHPublicKey(dhPubKeySpec.getY(),
                                       dhPubKeySpec.getP(),
                                       dhPubKeySpec.getG());

            } else if (keySpec instanceof X509EncodedKeySpec) {
                return new DHPublicKey
                    (((X509EncodedKeySpec)keySpec).getEncoded());

            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification");
        }
    }

    /**
     * Generates a private key object from the provided key specification
     * (key material).
     *
     * @param keySpec the specification (key material) of the private key
     *
     * @return the private key
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a private key.
     */
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        try {
            if (keySpec instanceof DHPrivateKeySpec) {
                DHPrivateKeySpec dhPrivKeySpec = (DHPrivateKeySpec)keySpec;
                return new DHPrivateKey(dhPrivKeySpec.getX(),
                                        dhPrivKeySpec.getP(),
                                        dhPrivKeySpec.getG());

            } else if (keySpec instanceof PKCS8EncodedKeySpec) {
                return new DHPrivateKey
                    (((PKCS8EncodedKeySpec)keySpec).getEncoded());

            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification");
        }
    }

    /**
     * Returns a specification (key material) of the given key object
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
     * @exception InvalidKeySpecException if the requested key specification is
     * inappropriate for the given key, or the given key cannot be processed
     * (e.g., the given key has an unrecognized algorithm or format).
     */
    protected KeySpec engineGetKeySpec(Key key, Class keySpec)
        throws InvalidKeySpecException {
        DHParameterSpec params;

        if (key instanceof javax.crypto.interfaces.DHPublicKey) {

            if (DHPublicKeySpec.class.isAssignableFrom(keySpec)) {
                javax.crypto.interfaces.DHPublicKey dhPubKey
                    = (javax.crypto.interfaces.DHPublicKey) key;
                params = dhPubKey.getParams();
                return new DHPublicKeySpec(dhPubKey.getY(),
                                           params.getP(),
                                           params.getG());

            } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new X509EncodedKeySpec(key.getEncoded());

            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }

        } else if (key instanceof javax.crypto.interfaces.DHPrivateKey) {

            if (DHPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                javax.crypto.interfaces.DHPrivateKey dhPrivKey
                    = (javax.crypto.interfaces.DHPrivateKey)key;
                params = dhPrivKey.getParams();
                return new DHPrivateKeySpec(dhPrivKey.getX(),
                                            params.getP(),
                                            params.getG());

            } else if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new PKCS8EncodedKeySpec(key.getEncoded());

            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }

        } else {
            throw new InvalidKeySpecException("Inappropriate key type");
        }
    }

    /**
     * Translates a key object, whose provider may be unknown or potentially
     * untrusted, into a corresponding key object of this key factory.
     *
     * @param key the key whose provider is unknown or untrusted
     *
     * @return the translated key
     *
     * @exception InvalidKeyException if the given key cannot be processed by
     * this key factory.
     */
    protected Key engineTranslateKey(Key key)
        throws InvalidKeyException
    {
        try {

            if (key instanceof javax.crypto.interfaces.DHPublicKey) {
                // Check if key originates from this factory
                if (key instanceof com.sun.crypto.provider.DHPublicKey) {
                    return key;
                }
                // Convert key to spec
                DHPublicKeySpec dhPubKeySpec
                    = (DHPublicKeySpec)engineGetKeySpec
                    (key, DHPublicKeySpec.class);
                // Create key from spec, and return it
                return engineGeneratePublic(dhPubKeySpec);

            } else if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
                // Check if key originates from this factory
                if (key instanceof com.sun.crypto.provider.DHPrivateKey) {
                    return key;
                }
                // Convert key to spec
                DHPrivateKeySpec dhPrivKeySpec
                    = (DHPrivateKeySpec)engineGetKeySpec
                    (key, DHPrivateKeySpec.class);
                // Create key from spec, and return it
                return engineGeneratePrivate(dhPrivKeySpec);

            } else {
                throw new InvalidKeyException("Wrong algorithm type");
            }

        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException("Cannot translate key");
        }
    }
}
