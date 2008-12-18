/*
 * Copyright 1994-1998 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.util;

/**
 * Thrown by the <code>nextElement</code> method of an 
 * <code>Enumeration</code> to indicate that there are no more 
 * elements in the enumeration. 
 *
 * @author  unascribed
 * @see     java.util.Enumeration
 * @see     java.util.Enumeration#nextElement()
 * @since   JDK1.0
 */
public
class NoSuchElementException extends RuntimeException {
    /**
     * Constructs a <code>NoSuchElementException</code> with <tt>null</tt> 
     * as its error message string.
     */
    public NoSuchElementException() {
	super();
    }

    /**
     * Constructs a <code>NoSuchElementException</code>, saving a reference 
     * to the error message string <tt>s</tt> for later retrieval by the 
     * <tt>getMessage</tt> method.
     *
     * @param   s   the detail message.
     */
    public NoSuchElementException(String s) {
	super(s);
    }
}
