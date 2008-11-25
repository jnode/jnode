/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.mirror.apt;


import com.sun.mirror.util.SourcePosition;


/**
 * A <tt>Messager</tt> provides the way for
 * an annotation processor to report error messages, warnings, and
 * other notices.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @since 1.5
 */

public interface Messager {

    /**
     * Prints an error message.
     * Equivalent to <tt>printError(null, msg)</tt>.
     * @param msg  the message, or an empty string if none
     */
    void printError(String msg);

    /**
     * Prints an error message.
     * @param pos  the position where the error occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printError(SourcePosition pos, String msg);

    /**
     * Prints a warning message.
     * Equivalent to <tt>printWarning(null, msg)</tt>.
     * @param msg  the message, or an empty string if none
     */
    void printWarning(String msg);

    /**
     * Prints a warning message.
     * @param pos  the position where the warning occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printWarning(SourcePosition pos, String msg);

    /**
     * Prints a notice.
     * Equivalent to <tt>printNotice(null, msg)</tt>.
     * @param msg  the message, or an empty string if none
     */
    void printNotice(String msg);

    /**
     * Prints a notice.
     * @param pos  the position where the noticed occured, or null if it is
     *			unknown or not applicable
     * @param msg  the message, or an empty string if none
     */
    void printNotice(SourcePosition pos, String msg);
}
