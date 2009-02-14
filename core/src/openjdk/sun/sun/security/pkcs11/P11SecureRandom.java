/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

import sun.security.pkcs11.wrapper.*;
import sun.security.pkcs11.wrapper.PKCS11Constants.*;

/**
 * SecureRandom implementation class. Some tokens support only
 * C_GenerateRandom() and not C_SeedRandom(). In order not to lose an
 * application specified seed, we create a SHA1PRNG that we mix with in that
 * case.
 *
 * Note that since SecureRandom is thread safe, we only need one
 * instance per PKCS#11 token instance. It is created on demand and cached
 * in the SunPKCS11 class.
 *
 * Also note that we obtain the PKCS#11 session on demand, no need to tie one
 * up.
 *
 * @author  Andreas Sterbenz
 * @since   1.5
 */
final class P11SecureRandom extends SecureRandomSpi {

    private static final long serialVersionUID = -8939510236124553291L;

    // token instance
    private final Token token;

    // PRNG for mixing, non-null if active (i.e. setSeed() has been called)
    private volatile SecureRandom mixRandom;

    // buffer, if mixing is used
    private byte[] mixBuffer;

    // bytes remaining in buffer, if mixing is used
    private int buffered;

    P11SecureRandom(Token token) {
        this.token = token;
    }

    // see JCA spec
    protected synchronized void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed must not be null");
        }
        Session session = null;
        try {
            session = token.getOpSession();
            token.p11.C_SeedRandom(session.id(), seed);
        } catch (PKCS11Exception e) {
            // cannot set seed
            // let a SHA1PRNG use that seed instead
            SecureRandom random = mixRandom;
            if (random != null) {
                random.setSeed(seed);
            } else {
                try {
                    mixBuffer = new byte[20];
                    random = SecureRandom.getInstance("SHA1PRNG");
                    // initialize object before assigning to class field
                    random.setSeed(seed);
                    mixRandom = random;
                } catch (NoSuchAlgorithmException ee) {
                    throw new ProviderException(ee);
                }
            }
        } finally {
            token.releaseSession(session);
        }
    }

    // see JCA spec
    protected void engineNextBytes(byte[] bytes) {
        if ((bytes == null) || (bytes.length == 0)) {
            return;
        }
        Session session = null;
        try {
            session = token.getOpSession();
            token.p11.C_GenerateRandom(session.id(), bytes);
            mix(bytes);
        } catch (PKCS11Exception e) {
            throw new ProviderException("nextBytes() failed", e);
        } finally {
            token.releaseSession(session);
        }
    }

    // see JCA spec
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] b = new byte[numBytes];
        engineNextBytes(b);
        return b;
    }

    private void mix(byte[] b) {
        SecureRandom random = mixRandom;
        if (random == null) {
            // avoid mixing if setSeed() has never been called
            return;
        }
        synchronized (this) {
            int ofs = 0;
            int len = b.length;
            while (len-- > 0) {
                if (buffered == 0) {
                    random.nextBytes(mixBuffer);
                    buffered = mixBuffer.length;
                }
                b[ofs++] ^= mixBuffer[mixBuffer.length - buffered];
                buffered--;
            }
        }
    }

}
