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

package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.SecretKey;

/**
 * Parameters for SSL/TLS key material generation.
 * This class is used to initialize KeyGenerator of the type
 * "TlsKeyMaterial". The keys returned by such KeyGenerators will be
 * instances of {@link TlsKeyMaterialSpec}.
 *
 * <p>Instances of this class are immutable.
 *
 * @since   1.6
 * @author  Andreas Sterbenz
 * @deprecated Sun JDK internal use only --- WILL BE REMOVED in Dolphin (JDK 7)
 */
@Deprecated
public class TlsKeyMaterialParameterSpec implements AlgorithmParameterSpec {

    private final SecretKey masterSecret;
    private final int majorVersion, minorVersion;
    private final byte[] clientRandom, serverRandom;
    private final String cipherAlgorithm;
    private final int cipherKeyLength, ivLength, macKeyLength;
    private final int expandedCipherKeyLength; // == 0 for domestic ciphersuites

    /**
     * Constructs a new TlsKeyMaterialParameterSpec.
     *
     * @param masterSecret the master secret
     * @param majorVersion the major number of the protocol version
     * @param minorVersion the minor number of the protocol version
     * @param clientRandom the client's random value
     * @param serverRandom the server's random value
     * @param cipherAlgorithm the algorithm name of the cipher keys to
     *    be generated
     * @param cipherKeyLength if 0, no cipher keys will be generated;
     *    otherwise, the length in bytes of cipher keys to be
     *    generated for domestic cipher suites; for cipher suites defined as
     *    exportable, the number of key material bytes to be generated;
     * @param expandedCipherKeyLength 0 for domestic cipher suites; for
     *    exportable cipher suites the length in bytes of the key to be
     *    generated.
     * @param ivLength the length in bytes of the initialization vector
     *    to be generated, or 0 if no initialization vector is required
     * @param macKeyLength the length in bytes of the MAC key to be generated
     *
     * @throws NullPointerException if masterSecret, clientRandom,
     *   serverRandom, or cipherAlgorithm are null
     * @throws IllegalArgumentException if the algorithm of masterSecret is
     *   not TlsMasterSecret, or if majorVersion or minorVersion are
     *   negative or larger than 255; or if cipherKeyLength, expandedKeyLength,
     *   ivLength, or macKeyLength are negative
     */
    public TlsKeyMaterialParameterSpec(SecretKey masterSecret,
            int majorVersion, int minorVersion, byte[] clientRandom,
            byte[] serverRandom, String cipherAlgorithm, int cipherKeyLength,
            int expandedCipherKeyLength, int ivLength, int macKeyLength) {
        if (masterSecret.getAlgorithm().equals("TlsMasterSecret") == false) {
            throw new IllegalArgumentException("Not a TLS master secret");
        }
        if (cipherAlgorithm == null) {
            throw new NullPointerException();
        }
        this.masterSecret = masterSecret;
        this.majorVersion = TlsMasterSecretParameterSpec.checkVersion(majorVersion);
        this.minorVersion = TlsMasterSecretParameterSpec.checkVersion(minorVersion);
        this.clientRandom = clientRandom.clone();
        this.serverRandom = serverRandom.clone();
        this.cipherAlgorithm = cipherAlgorithm;
        this.cipherKeyLength = checkSign(cipherKeyLength);
        this.expandedCipherKeyLength = checkSign(expandedCipherKeyLength);
        this.ivLength = checkSign(ivLength);
        this.macKeyLength = checkSign(macKeyLength);
    }

    private static int checkSign(int k) {
        if (k < 0) {
            throw new IllegalArgumentException("Value must not be negative");
        }
        return k;
    }

    /**
     * Returns the master secret.
     *
     * @return the master secret.
     */
    public SecretKey getMasterSecret() {
        return masterSecret;
    }

    /**
     * Returns the major version number.
     *
     * @return the major version number.
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version number.
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Returns a copy of the client's random value.
     *
     * @return a copy of the client's random value.
     */
    public byte[] getClientRandom() {
        return clientRandom.clone();
    }

    /**
     * Returns a copy of the server's random value.
     *
     * @return a copy of the server's random value.
     */
    public byte[] getServerRandom() {
        return serverRandom.clone();
    }

    /**
     * Returns the cipher algorithm.
     *
     * @return the cipher algorithm.
     */
    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    /**
     * Returns the length in bytes of the encryption key to be generated.
     *
     * @return the length in bytes of the encryption key to be generated.
     */
    public int getCipherKeyLength() {
        return cipherKeyLength;
    }

    /**
     * Returns the length in bytes of the expanded encryption key to be generated.
     *
     * @return the length in bytes of the expanded encryption key to be generated.
     */
    public int getExpandedCipherKeyLength() {
        return expandedCipherKeyLength;
    }

    /**
     * Returns the length in bytes of the initialization vector to be generated.
     *
     * @return the length in bytes of the initialization vector to be generated.
     */
    public int getIvLength() {
        return ivLength;
    }

    /**
     * Returns the length in bytes of the MAC key to be generated.
     *
     * @return the length in bytes of the MAC key to be generated.
     */
    public int getMacKeyLength() {
        return macKeyLength;
    }

}
