/*
 * Copyright 2001-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.crypto.interfaces;

import java.math.BigInteger;

/**
 * The interface to a PBE key.
 *
 * @author Valerie Peng
 *
 * @see javax.crypto.spec.PBEKeySpec
 * @see javax.crypto.SecretKey
 * @since 1.4
 */
public interface PBEKey extends javax.crypto.SecretKey {

    /**
     * The class fingerprint that is set to indicate serialization
     * compatibility since J2SE 1.4.
     */
    static final long serialVersionUID = -1430015993304333921L;

    /**
     * Returns the password.
     *
     * <p> Note: this method should return a copy of the password. It is
     * the caller's responsibility to zero out the password information after
     * it is no longer needed.
     *
     * @return the password.
     */
    char[] getPassword();

    /**
     * Returns the salt or null if not specified.
     *
     * <p> Note: this method should return a copy of the salt. It is
     * the caller's responsibility to zero out the salt information after
     * it is no longer needed.
     *
     * @return the salt.
     */
    byte[] getSalt();

    /**
     * Returns the iteration count or 0 if not specified.
     *
     * @return the iteration count.
     */
    int getIterationCount();
}
