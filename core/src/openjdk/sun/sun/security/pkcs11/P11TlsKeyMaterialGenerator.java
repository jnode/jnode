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

package sun.security.pkcs11;

import java.util.*;

import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.*;
import javax.crypto.spec.*;

import sun.security.internal.spec.*;
import sun.security.internal.interfaces.TlsMasterSecret;

import static sun.security.pkcs11.TemplateManager.*;
import sun.security.pkcs11.wrapper.*;
import static sun.security.pkcs11.wrapper.PKCS11Constants.*;

/**
 * KeyGenerator to calculate the SSL/TLS key material (cipher keys and ivs,
 * mac keys) from the master secret.
 *
 * @author  Andreas Sterbenz
 * @since   1.6
 */
public final class P11TlsKeyMaterialGenerator extends KeyGeneratorSpi {

    private final static String MSG = "TlsKeyMaterialGenerator must be "
        + "initialized using a TlsKeyMaterialParameterSpec";

    // token instance
    private final Token token;

    // algorithm name
    private final String algorithm;

    // mechanism id
    private long mechanism;

    // parameter spec
    private TlsKeyMaterialParameterSpec spec;

    // master secret as a P11Key
    private P11Key p11Key;

    // version, e.g. 0x0301
    private int version;

    P11TlsKeyMaterialGenerator(Token token, String algorithm, long mechanism)
            throws PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
    }

    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof TlsKeyMaterialParameterSpec == false) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsKeyMaterialParameterSpec)params;
        try {
            p11Key = P11SecretKeyFactory.convertKey
                            (token, spec.getMasterSecret(), "TlsMasterSecret");
        } catch (InvalidKeyException e) {
            throw new InvalidAlgorithmParameterException("init() failed", e);
        }
        version = (spec.getMajorVersion() << 8) | spec.getMinorVersion();
        if ((version < 0x0300) && (version > 0x0302)) {
            throw new InvalidAlgorithmParameterException
                    ("Only SSL 3.0, TLS 1.0, and TLS 1.1 are supported");
        }
        // we assume the token supports both the CKM_SSL3_* and the CKM_TLS_*
        // mechanisms
    }

    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }

    protected SecretKey engineGenerateKey() {
        if (spec == null) {
            throw new IllegalStateException
                ("TlsKeyMaterialGenerator must be initialized");
        }
        mechanism = (version == 0x0300) ? CKM_SSL3_KEY_AND_MAC_DERIVE
                                         : CKM_TLS_KEY_AND_MAC_DERIVE;
        int macBits = spec.getMacKeyLength() << 3;
        int ivBits = spec.getIvLength() << 3;

        int expandedKeyBits = spec.getExpandedCipherKeyLength() << 3;
        int keyBits = spec.getCipherKeyLength() << 3;
        boolean isExportable;
        if (expandedKeyBits != 0) {
            isExportable = true;
        } else {
            isExportable = false;
            expandedKeyBits = keyBits;
        }

        CK_SSL3_RANDOM_DATA random = new CK_SSL3_RANDOM_DATA
                            (spec.getClientRandom(), spec.getServerRandom());
        CK_SSL3_KEY_MAT_PARAMS params = new CK_SSL3_KEY_MAT_PARAMS
                            (macBits, keyBits, ivBits, isExportable, random);

        String cipherAlgorithm = spec.getCipherAlgorithm();
        long keyType = P11SecretKeyFactory.getKeyType(cipherAlgorithm);
        if (keyType < 0) {
            if (keyBits != 0) {
                throw new ProviderException
                            ("Unknown algorithm: " + spec.getCipherAlgorithm());
            } else {
                // NULL encryption ciphersuites
                keyType = CKK_GENERIC_SECRET;
            }
        }

        Session session = null;
        try {
            session = token.getObjSession();
            CK_ATTRIBUTE[] attributes;
            if (keyBits != 0) {
                attributes = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                    new CK_ATTRIBUTE(CKA_KEY_TYPE, keyType),
                    new CK_ATTRIBUTE(CKA_VALUE_LEN, expandedKeyBits >> 3),
                };
            } else {
                // ciphersuites with NULL ciphers
                attributes = new CK_ATTRIBUTE[0];
            }
            attributes = token.getAttributes
                (O_GENERATE, CKO_SECRET_KEY, keyType, attributes);
            // the returned keyID is a dummy, ignore
            long keyID = token.p11.C_DeriveKey(session.id(),
                new CK_MECHANISM(mechanism, params), p11Key.keyID, attributes);

            CK_SSL3_KEY_MAT_OUT out = params.pReturnedKeyMaterial;
            // Note that the MAC keys do not inherit all attributes from the
            // template, but they do inherit the sensitive/extractable/token
            // flags, which is all P11Key cares about.
            SecretKey clientMacKey = P11Key.secretKey
                    (session, out.hClientMacSecret, "MAC", macBits, attributes);
            SecretKey serverMacKey = P11Key.secretKey
                    (session, out.hServerMacSecret, "MAC", macBits, attributes);
            SecretKey clientCipherKey, serverCipherKey;
            if (keyBits != 0) {
                clientCipherKey = P11Key.secretKey(session, out.hClientKey,
                        cipherAlgorithm, expandedKeyBits, attributes);
                serverCipherKey = P11Key.secretKey(session, out.hServerKey,
                        cipherAlgorithm, expandedKeyBits, attributes);
            } else {
                clientCipherKey = null;
                serverCipherKey = null;
            }
            IvParameterSpec clientIv = (out.pIVClient == null)
                                    ? null : new IvParameterSpec(out.pIVClient);
            IvParameterSpec serverIv = (out.pIVServer == null)
                                    ? null : new IvParameterSpec(out.pIVServer);

            return new TlsKeyMaterialSpec(clientMacKey, serverMacKey,
                    clientCipherKey, clientIv, serverCipherKey, serverIv);

        } catch (Exception e) {
            throw new ProviderException("Could not generate key", e);
        } finally {
            token.releaseSession(session);
        }
    }

}
