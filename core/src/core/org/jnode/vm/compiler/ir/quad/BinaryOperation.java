/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.compiler.ir.quad;

public enum BinaryOperation {
    IADD("+"),
    LADD("+"),
    FADD("+"),
    DADD("+"),
    ISUB("-"),
    LSUB("-"),
    FSUB("-"),
    DSUB("-"),
    IMUL("*"),
    LMUL("*"),
    FMUL("*"),
    DMUL("*"),
    IDIV("/"),
    LDIV("/"),
    FDIV("/"),
    DDIV("/"),
    IREM("%"),
    LREM("%"),
    FREM("%"),
    DREM("%"),
    ISHL("<<"),
    LSHL("<<"),
    ISHR(">>"),
    LSHR(">>"),
    IUSHR(">>>"),
    LUSHR(">>>"),
    IAND("&"),
    LAND("&"),
    IOR("|"),
    LOR("|"),
    IXOR("^"),
    LXOR("^");
    
    private final String v;
    private BinaryOperation(String v) {
        this.v = v;
    }
    
    public final String getOperation() {
        return v;
    }
}
