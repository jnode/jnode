/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.vm.compiler.ir.quad;

public enum BranchCondition {

    IFEQ("== 0", false), IFNE("!= 0", false), IFLT("< 0", false), IFGE(
    ">= 0", false), IFGT("> 0", false), IFLE("<= 0", false),

    IFNONNULL("!= null", false), IFNULL("== null", false),

    IF_ICMPEQ("==", true), IF_ICMPNE("!=", true), IF_ICMPLT("<", true), IF_ICMPGE(
    ">=", true), IF_ICMPGT(">", true), IF_ICMPLE("<=", true),

    IF_ACMPEQ("==", true), IF_ACMPNE("!=", true);

    private final String v;

    final boolean binary;

    private BranchCondition(String v, boolean binary) {
        this.v = v;
        this.binary = binary;
    }

    public String getCondition() {
        return v;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isUnary() {
        return !binary;
    }
}
