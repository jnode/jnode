/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.vm.JvmType;

/**
 * Bytecode to native code compiler for floating point instructions.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class FPCompiler {

    protected final X86BytecodeVisitor bcv;
    protected final X86Assembler os;
    protected final EmitterContext ec;
    protected final VirtualStack vstack;
    protected final int arrayDataOffset;

    /**
     * Create a constant item with a floating point value. Constant value will
     * be converted to the given type first.
     *
     * @param type
     * @param value
     * @return The new constant item
     */
    protected final Item createConst(ItemFactory ifac, int type, double value) {
        switch (type) {
            case JvmType.DOUBLE:
                return ifac.createDConst(ec, value);
            case JvmType.FLOAT:
                return ifac.createFConst(ec, (float) value);
            case JvmType.INT:
                return ifac.createIConst(ec, (int) value);
            case JvmType.LONG:
                return ifac.createLConst(ec, (long) value);
            default:
                throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    protected static double getFPValue(Item item) {
        switch (item.getType()) {
            case JvmType.INT:
                return ((IntItem) item).getValue();
            case JvmType.LONG:
                return ((LongItem) item).getValue();
            case JvmType.FLOAT:
                return ((FloatItem) item).getValue();
            case JvmType.DOUBLE:
                return ((DoubleItem) item).getValue();
            default:
                throw new InternalError(" Cannot get FP value of item with type "
                    + item.getType());
        }
    }

    public FPCompiler(X86BytecodeVisitor bcv, X86Assembler os, EmitterContext ec, VirtualStack vstack,
                      int arrayDataOffset) {
        this.bcv = bcv;
        this.os = os;
        this.ec = ec;
        this.vstack = vstack;
        this.arrayDataOffset = arrayDataOffset;
    }

    /**
     * fadd / dadd
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void add(int type);

    /**
     * fcmpg, fcmpl, dcmpg, dcmpl
     *
     * @param os
     * @param ec
     * @param vstack
     * @param gt
     * @param type
     * @param curInstrLabel
     */
    abstract void compare(boolean gt, int type, Label curInstrLabel);

    /**
     * f2x / d2x
     *
     * @param ec
     * @param vstack
     */
    abstract void convert(int fromType, int toType);

    /**
     * fdiv / ddiv
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void div(int type);

    /**
     * fmul / dmul
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void mul(int type);

    /**
     * fneg / dneg
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void neg(int type);

    /**
     * frem / drem
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void rem(int type);

    /**
     * fsub / dsub
     *
     * @param ec
     * @param vstack
     * @param type
     */
    abstract void sub(int type);

    /**
     * faload / daload
     *
     * @param type
     */
    abstract void fpaload(int type);
}
