/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.security.auth;

/**
 * Objects such as credentials may optionally implement this interface
 * to provide the capability to destroy its contents.
 * 
 * @see javax.security.auth.Subject
 */
public interface Destroyable {

    /**
     * Destroy this <code>Object</code>.
     *
     * <p> Sensitive information associated with this <code>Object</code>
     * is destroyed or cleared.  Subsequent calls to certain methods
     * on this <code>Object</code> will result in an
     * <code>IllegalStateException</code> being thrown.
     *
     * <p>
     *
     * @exception DestroyFailedException if the destroy operation fails. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *		to destroy this <code>Object</code>.
     */
    void destroy() throws DestroyFailedException;

    /**
     * Determine if this <code>Object</code> has been destroyed.
     *
     * <p>
     *
     * @return true if this <code>Object</code> has been destroyed,
     *		false otherwise.
     */
    boolean isDestroyed();
}
