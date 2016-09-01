/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

public enum BinaryOperation {
    IADD("+"),
    ISUB("-"),
    IMUL("*"),
    IDIV("/"),
    IREM("%"),
    ISHL("<<"),
    ISHR(">>"),
    IUSHR(">>>"),
    IAND("&"),
    IOR("|"),
    IXOR("^"),

    LADD("+"),
    LSUB("-"),
    LMUL("*"),
    LDIV("/"),
    LREM("%"),
    LSHL("<<"),
    LSHR(">>"),
    LUSHR(">>>"),
    LAND("&"),
    LOR("|"),
    LXOR("^"),
    LCMP("lcmp"),

    FADD("+"),
    FSUB("-"),
    FMUL("*"),
    FDIV("/"),
    FREM("%"),
    FCMPG("fcmpg"),
    FCMPL("fcmpl"),

    DADD("+"),
    DSUB("-"),
    DMUL("*"),
    DDIV("/"),
    DREM("%"),
    DCMPG("dcmpg"),
    DCMPL("dcmpl");

    private final String v;

    private BinaryOperation(String v) {
        this.v = v;
    }

    public final String getOperation() {
        return v;
    }
}
