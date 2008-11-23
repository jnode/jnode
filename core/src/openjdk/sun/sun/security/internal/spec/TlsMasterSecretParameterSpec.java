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
 * Parameters for SSL/TLS master secret generation.
 * This class encapsulates the information necessary to calculate a SSL/TLS
 * master secret from the premaster secret and other parameters.
 * It is used to initialize KeyGenerators of the type "TlsMasterSecret".
 *
 * <p>Instances of this class are immutable.
 *
 * @since   1.6
 * @author  Andreas Sterbenz
 * @deprecated Sun JDK internal use only --- WILL BE REMOVED in Dolphin (JDK 7)
 */
@Deprecated
public class TlsMasterSecretParameterSpec implements AlgorithmParameterSpec {

    private final SecretKey premasterSecret;
    private final int majorVersion, minorVersion;
    private final byte[] clientRandom, serverRandom;

    /**
     * Constructs a new TlsMasterSecretParameterSpec.
     *
     * <p>The <code>getAlgorithm()</code> method of <code>premasterSecret</code>
     * should return <code>"TlsRsaPremasterSecret"</code> if the key exchange
     * algorithm was RSA and <code>"TlsPremasterSecret"</code> otherwise.
     *
     * @param premasterSecret the premaster secret
     * @param majorVersion the major number of the protocol version
     * @param minorVersion the minor number of the protocol version
     * @param clientRandom the client's random value
     * @param serverRandom the server's random value
     *
     * @throws NullPointerException if premasterSecret, clientRandom,
     *   or serverRandom are null
     * @throws IllegalArgumentException if minorVersion or majorVersion are
     *   negative or larger than 255
     */
    public TlsMasterSecretParameterSpec(SecretKey premasterSecret,
            int majorVersion, int minorVersion, byte[] clientRandom, byte[] serverRandom) {
        if (premasterSecret == null) {
            throw new NullPointerException("premasterSecret must not be null");
        }
        this.premasterSecret = premasterSecret;
        this.majorVersion = checkVersion(majorVersion);
        this.minorVersion = checkVersion(minorVersion);
        this.clientRandom = clientRandom.clone();
        this.serverRandom = serverRandom.clone();
    }

    static int checkVersion(int version) {
        if ((version < 0) || (version > 255)) {
            throw new IllegalArgumentException("Version must be between 0 and 255");
        }
        return version;
    }

    /**
     * Returns the premaster secret.
     *
     * @return the premaster secret.
     */
    public SecretKey getPremasterSecret() {
        return premasterSecret;
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

}
