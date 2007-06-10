/*
 * Copyright 1994-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.tools.tree;

import sun.tools.java.*;
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class IntExpression extends IntegerExpression {
    /**
     * Constructor
     */
    public IntExpression(long where, int value) {
	super(INTVAL, where, Type.tInt, value);
    }

    /**
     * Equality, this is needed so that switch statements
     * can put IntExpressions in a hashtable
     */
    public boolean equals(Object obj) {
	if ((obj != null) && (obj instanceof IntExpression)) {
	    return value == ((IntExpression)obj).value;
	}
	return false;
    }

    /**
     * Hashcode, this is needed so that switch statements
     * can put IntExpressions in a hashtable
     */
    public int hashCode() {
	return value;
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
	out.print(value);
    }
}
