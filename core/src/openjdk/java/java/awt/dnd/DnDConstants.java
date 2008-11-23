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
 * This class contains constant values representing
 * the type of action(s) to be performed by a Drag and Drop operation.
 * @since 1.2
 */

public final class DnDConstants {

    private DnDConstants() {} // define null private constructor.

    /**
     * An <code>int</code> representing no action. 
     */
    public static final int ACTION_NONE		= 0x0;

    /**
     * An <code>int</code> representing a &quot;copy&quot; action.
     */
    public static final int ACTION_COPY		= 0x1;

    /**
     * An <code>int</code> representing a &quot;move&quot; action.
     */
    public static final int ACTION_MOVE		= 0x2;

    /**
     * An <code>int</code> representing a &quot;copy&quot; or 
     * &quot;move&quot; action.
     */
    public static final int ACTION_COPY_OR_MOVE	= ACTION_COPY | ACTION_MOVE;

    /**
     * An <code>int</code> representing a &quot;link&quot; action.
     *
     * The link verb is found in many, if not all native DnD platforms, and the
     * actual interpretation of LINK semantics is both platform
     * and application dependent. Broadly speaking, the
     * semantic is "do not copy, or move the operand, but create a reference
     * to it". Defining the meaning of "reference" is where ambiguity is
     * introduced.
     *
     * The verb is provided for completeness, but its use is not recommended
     * for DnD operations between logically distinct applications where 
     * misinterpretation of the operations semantics could lead to confusing
     * results for the user.
     */

    public static final int ACTION_LINK	        = 0x40000000;

    /**
     * An <code>int</code> representing a &quot;reference&quot; 
     * action (synonym for ACTION_LINK).
     */
    public static final int ACTION_REFERENCE    = ACTION_LINK;

}
