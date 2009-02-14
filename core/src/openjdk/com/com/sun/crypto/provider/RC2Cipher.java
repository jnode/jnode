/*
 * Copyright 2003-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.RC2ParameterSpec;

/**
 * JCE CipherSpi for the RC2(tm) algorithm as described in RFC 2268.
 * The real code is in CipherCore and RC2Crypt.
 *
 * @since   1.5
 * @author  Andreas Sterbenz
 */
public final class RC2Cipher extends CipherSpi {

    // internal CipherCore & RC2Crypt objects which do the real work.
    private final CipherCore core;
    private final RC2Crypt embeddedCipher;

    public RC2Cipher() {
        SunJCE.ensureIntegrity(getClass());
        embeddedCipher = new RC2Crypt();
        core = new CipherCore(embeddedCipher, 8);
    }

    protected void engineSetMode(String mode)
            throws NoSuchAlgorithmException {
        core.setMode(mode);
    }

    protected void engineSetPadding(String paddingScheme)
            throws NoSuchPaddingException {
        core.setPadding(paddingScheme);
    }

    protected int engineGetBlockSize() {
        return 8;
    }

    protected int engineGetOutputSize(int inputLen) {
        return core.getOutputSize(inputLen);
    }

    protected byte[] engineGetIV() {
        return core.getIV();
    }

    protected AlgorithmParameters engineGetParameters() {
        return core.getParameters("RC2");
    }

    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        embeddedCipher.initEffectiveKeyBits(0);
        core.init(opmode, key, random);
    }

    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null && params instanceof RC2ParameterSpec) {
            embeddedCipher.initEffectiveKeyBits
                (((RC2ParameterSpec)params).getEffectiveKeyBits());
        } else {
            embeddedCipher.initEffectiveKeyBits(0);
        }
        core.init(opmode, key, params, random);
    }

    protected void engineInit(int opmode, Key key,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null && params.getAlgorithm().equals("RC2")) {
            try {
                RC2ParameterSpec rc2Params = (RC2ParameterSpec)
                    params.getParameterSpec(RC2ParameterSpec.class);
                engineInit(opmode, key, rc2Params, random);
            } catch (InvalidParameterSpecException ipse) {
                throw new InvalidAlgorithmParameterException
                            ("Wrong parameter type: RC2 expected");
            }
        } else {
            embeddedCipher.initEffectiveKeyBits(0);
            core.init(opmode, key, params, random);
        }
    }

    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        return core.update(in, inOfs, inLen);
    }

    protected int engineUpdate(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException {
        return core.update(in, inOfs, inLen, out, outOfs);
    }

    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen)
            throws IllegalBlockSizeException, BadPaddingException {
        return core.doFinal(in, inOfs, inLen);
    }

    protected int engineDoFinal(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws IllegalBlockSizeException,
            ShortBufferException, BadPaddingException {
        return core.doFinal(in, inOfs, inLen, out, outOfs);
    }

    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] keyBytes = CipherCore.getKeyBytes(key);
        RC2Crypt.checkKey(key.getAlgorithm(), keyBytes.length);
        return keyBytes.length << 3;
    }

    protected byte[] engineWrap(Key key)
            throws IllegalBlockSizeException, InvalidKeyException {
        return core.wrap(key);
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm,
            int wrappedKeyType) throws InvalidKeyException,
            NoSuchAlgorithmException {
        return core.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
    }

}
