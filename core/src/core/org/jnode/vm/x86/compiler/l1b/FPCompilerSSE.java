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

package org.jnode.vm.x86.compiler.l1b;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.JvmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPCompilerSSE extends FPCompiler {

    /**
     * @param bcv
     * @param os
     * @param ec
     * @param vstack
     * @param arrayDataOffset
     */
    public FPCompilerSSE(X86BytecodeVisitor bcv, X86Assembler os,
                         EmitterContext ec, VirtualStack vstack, int arrayDataOffset) {
        super(bcv, os, ec, vstack, arrayDataOffset);
    }

    final void add(int type) {
        arithOperation(type, X86Operation.SSE_ADD, true);
    }

    void compare(boolean gt, int type, Label curInstrLabel) {
        // TODO Auto-generated method stub

    }

    void convert(int fromType, int toType) {
        // TODO Auto-generated method stub

    }

    final void div(int type) {
        arithOperation(type, X86Operation.SSE_DIV, false);
    }

    void fpaload(int type) {
        // TODO Auto-generated method stub

    }

    final void mul(int type) {
        arithOperation(type, X86Operation.SSE_MUL, true);
    }

    void neg(int type) {
        // TODO Auto-generated method stub

    }

    final void rem(int type) {
        // TODO implement me
        //arithOperation(type, X86Operation.SSE_REM, false);
    }

    final void sub(int type) {
        arithOperation(type, X86Operation.SSE_SUB, false);
    }

    /**
     * Generate code for an SSE arithmatic operation.
     *
     * @param type
     * @param operation
     * @param commutative
     */
    private final void arithOperation(int type, int operation, boolean commutative) {
        final ItemFactory ifac = ec.getItemFactory();
        Item v2 = vstack.pop(type);
        Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 + fpv2));
        } else {
            if (prepareForOperation(v1, v2, commutative)) {
                // Swap
                final Item tmp = v2;
                v2 = v1;
                v1 = tmp;
            }
            final X86Register.XMM r1 = v1.getXMM();
            switch (v2.getKind()) {
                case Item.Kind.LOCAL:
                    if (type == JvmType.FLOAT) {
                        os.writeArithSSESOp(operation, r1, X86Register.EBP, v2.getOffsetToFP(ec));
                    } else {
                        os.writeArithSSEDOp(operation, r1, X86Register.EBP, v2.getOffsetToFP(ec));
                    }
                    break;
                default:
                    if (type == JvmType.FLOAT) {
                        os.writeArithSSESOp(operation, r1, v2.getXMM());
                    } else {
                        os.writeArithSSEDOp(operation, r1, v2.getXMM());
                    }
            }
            v2.release(ec);
            vstack.push(v1);
        }
    }

    /**
     * Prepare both operand for operand. At least one operand is loaded into a
     * register. The other operand is constant, local or register.
     *
     * @param destAndSource
     * @param source
     * @param commutative
     * @return True if the operand must be swapped. when not commutative, false
     *         is always returned.
     */
    private final boolean prepareForOperation(Item destAndSource, Item source,
                                              boolean commutative) {
        // WARNING: source was on top of the virtual stack (thus higher than
        // destAndSource)
        // x86 can only deal with one complex argument
        // destAndSource must be a register
        source.loadToXMMIf(ec, ~Item.Kind.LOCAL);
        destAndSource.loadToXMM(ec);
        return false;
    }
}
