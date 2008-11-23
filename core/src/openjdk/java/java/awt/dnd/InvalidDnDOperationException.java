/*
 * Copyright 1997-1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.awt.dnd;

/**
 * This exception is thrown by various methods in the java.awt.dnd package.
 * It is usually thrown to indicate that the target in question is unable
 * to undertake the requested operation that the present time, since the
 * undrelying DnD system is not in the appropriate state.
 *
 * @since 1.2
 */

public class InvalidDnDOperationException extends IllegalStateException {
    
    static private String dft_msg = "The operation requested cannot be performed by the DnD system since it is not in the appropriate state";

    /**
     * Create a default Exception
     */

    public InvalidDnDOperationException() { super(dft_msg); }

    /**
     * Create an Exception with its own descriptive message
     * <P>
     * @param msg the detail message
     */

    public InvalidDnDOperationException(String msg) { super(msg); }

}
