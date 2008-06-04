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
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.FPU;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.system.BootLog;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPCompilerFPU extends FPCompiler {

    /**
     * @param os
     * @param ec
     * @param vstack
     */
    public FPCompilerFPU(X86BytecodeVisitor bcv, X86Assembler os,
                         EmitterContext ec, VirtualStack vstack, int arrayDataOffset) {
        super(bcv, os, ec, vstack, arrayDataOffset);
    }

    /**
     * fadd / dadd
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void add(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 + fpv2));
            v1.release(ec);
            v2.release(ec);
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, true);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFADDP(reg);

            // Push result
            vstack.push(result);
        }
    }

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
    final void compare(boolean gt, int type, Label curInstrLabel) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        // Prepare operands
        final FPUStack fpuStack = vstack.fpuStack;
        final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
        // We need reg to be ST1.
        fxchST1(os, fpuStack, reg);

        final X86RegisterPool pool = ec.getGPRPool();
        pool.request(X86Register.EAX);
        final IntItem result = (IntItem) L1AHelper.requestWordRegister(ec,
            JvmType.INT, false);
        final GPR resr = result.getRegister();

        // Clear resultr
        os.writeXOR(resr, resr);

        if (!gt) {
            // Reverse order
            FPUHelper.fxch(os, fpuStack, X86Register.ST1);
        }
        os.writeFUCOMPP(); // Compare, Pop twice
        os.writeFNSTSW_AX(); // Store fp status word in AX
        if (os.isCode32()) {
            os.writeSAHF(); // Store AH to Flags
        }

        // Pop fpu stack twice (FUCOMPP)
        fpuStack.pop();
        fpuStack.pop();

        final Label gtLabel = new Label(curInstrLabel + "gt");
        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");
        if (os.isCode32()) {
            os.writeJCC(gtLabel, X86Constants.JA);
            os.writeJCC(ltLabel, X86Constants.JB);
        } else {
            // Emulate JA
            os.writeTEST(X86Register.EAX, (X86Constants.F_CF | X86Constants.F_ZF) << 8);
            os.writeJCC(gtLabel, X86Constants.JZ);
            // Emulate JB
            os.writeTEST(X86Register.EAX, (X86Constants.F_CF) << 8);
            os.writeJCC(ltLabel, X86Constants.JNZ);
        }
        os.writeJMP(endLabel); // equal
        // Greater
        os.setObjectRef(gtLabel);
        if (gt) {
            os.writeDEC(resr);
        } else {
            os.writeINC(resr);
        }
        os.writeJMP(endLabel);
        // Less
        os.setObjectRef(ltLabel);
        if (gt) {
            os.writeINC(resr);
        } else {
            os.writeDEC(resr);
        }
        // End
        os.setObjectRef(endLabel);

        // Push result
        vstack.push(result);

        // Release
        pool.release(X86Register.EAX);
    }

    /**
     * f2x / d2x
     *
     * @param ec
     * @param vstack
     */
    final void convert(int fromType, int toType) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v = vstack.pop(fromType);
        if (v.isConstant()) {
            vstack.push(createConst(ifac, toType, getFPValue(v)));
            v.release(ec);
        } else {
            v.pushToFPU(ec);
            vstack.fpuStack.pop(v);
            v.release(ec);
            final Item result = ifac.createFPUStack(toType);
            vstack.push(result);
            vstack.fpuStack.push(result);

            // Now load to GPR (to force conversion)
            result.loadToGPR(ec);
        }
    }

    /**
     * fdiv / ddiv
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void div(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 / fpv2));
            v1.release(ec);
            v2.release(ec);
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFDIVP(reg);

            // Push result
            vstack.push(result);
        }
    }

    /**
     * Ensure that there are at least items registers left on the FPU stack. If
     * the number of items is not free, the stack is flushed onto the CPU stack.
     *
     * @param os
     * @param ec
     * @param vstack
     * @param items
     */
    static final void ensureStackCapacity(X86Assembler os, EmitterContext ec,
                                          VirtualStack vstack, int items) {
        final FPUStack fpuStack = vstack.fpuStack;
        if (!fpuStack.hasCapacity(items)) {
            BootLog.debug("Flush FPU stack;\n  fpuStack=" + fpuStack
                + ",\n  vstack  =" + vstack);
            vstack.push(ec);
            if (Vm.VerifyAssertions)
                Vm._assert(fpuStack.hasCapacity(items), "Out of FPU stack");
        }
    }

    /**
     * Swap the given register and ST1 unless the given register is already ST1.
     *
     * @param os
     * @param fpuStack
     * @param fpuReg
     */
    private static final void fxchST1(X86Assembler os, FPUStack fpuStack,
                                      FPU fpuReg) {
        // We need reg to be ST1, if not swap
        if (fpuReg != X86Register.ST1) {
            // Swap reg with ST0
            FPUHelper.fxch(os, fpuStack, fpuReg);
            FPUHelper.fxch(os, fpuStack, X86Register.ST1);
            FPUHelper.fxch(os, fpuStack, fpuReg);
        }
    }

    /**
     * fmul / dmul
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void mul(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 * fpv2));
            v1.release(ec);
            v2.release(ec);
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, true);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFMULP(reg);

            // Push result
            vstack.push(result);
        }
    }

    /**
     * fneg / dneg
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void neg(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v = vstack.pop(type);
        if (v.isConstant()) {
            final double fpv = getFPValue(v);
            vstack.push(createConst(ifac, type, -fpv));
            v.release(ec);
        } else {
            // Prepare
            final FPUStack fpuStack = vstack.fpuStack;
            prepareForOperation(os, ec, vstack, fpuStack, v);

            // Calculate
            os.writeFCHS();

            // Push result
            vstack.push(v);
        }
    }

    /**
     * Make sure that the given operand is on the top on the FPU stack.
     */
    private static void prepareForOperation(X86Assembler os,
                                                  EmitterContext ec, VirtualStack vstack, FPUStack fpuStack,
                                                  Item left) {
        final boolean onFpu = left.isFPUStack();

        // If the FPU stack will be full in this operation, we flush the vstack
        // first.
        int extraItems = onFpu ? 0 : 1;
        ensureStackCapacity(os, ec, vstack, extraItems);

        if (onFpu) {
            // Operand is on the FPU stack
            if (!fpuStack.isTos(left)) {
                // operand not on top, exchange it.
                final FPU reg = fpuStack.getRegister(left);
                os.writeFXCH(reg);
                fpuStack.fxch(reg);
            }
        } else {
            // operand is not on FPU stack
            left.pushToFPU(ec); // Now left is on top
        }
    }

    /**
     * Make sure both operand are on the FPU stack, on of which is in ST0. The
     * FPU register of the other operand is returned. If commutative is true,
     * either left of right will be located in ST0, if commutative is false, the
     * left operand will be in ST0.
     * <p/>
     * The item at ST0 is popped of the given fpuStack stack.
     *
     * @param left
     * @param right
     */
    private static FPU prepareForOperation(X86Assembler os,
                                                 EmitterContext ec, VirtualStack vstack, FPUStack fpuStack,
                                                 Item left, Item right, boolean commutative) {
        final boolean lOnFpu = left.isFPUStack();
        final boolean rOnFpu = right.isFPUStack();
        final FPU reg;

        // If the FPU stack will be full in this operation, we flush the vstack
        // first.
        int extraItems = 0;
        extraItems += lOnFpu ? 0 : 1;
        extraItems += rOnFpu ? 0 : 1;
        ensureStackCapacity(os, ec, vstack, extraItems);

        if (lOnFpu && rOnFpu) {
            // Both operand are on the FPU stack
            if (fpuStack.isTos(left)) {
                // Left already on top
                reg = fpuStack.getRegister(right);
            } else if (fpuStack.isTos(right)) {
                // Right on top
                reg = fpuStack.getRegister(left);
                if (commutative) {
                    // Commutative so return left register
                } else {
                    // Non-commutative, so swap left-right and return left
                    // register
                    // System.out.println("stack: " + fpuStack + "\nleft: " +
                    // left + "\nright: " + right + "\nreg: " + reg);
                    FPUHelper.fxch(os, fpuStack, reg);
                }
            } else {
                // Neither left not right on top
                FPUHelper.fxch(os, fpuStack, fpuStack.getRegister(left)); // Swap left &
                // ST0, now left
                // is on top
                reg = fpuStack.getRegister(right);
            }
        } else if (!(lOnFpu || rOnFpu)) {
            // Neither operands are on the FPU stack
            left.pushToFPU(ec);
            right.pushToFPU(ec); // Now right is on top
            reg = X86Register.ST1; // Left is just below top
            if (!commutative) {
                FPUHelper.fxch(os, fpuStack, reg);
            }
        } else if (lOnFpu) {
            // Left operand is on FPU stack, right is not
            right.pushToFPU(ec); // Now right is on top
            // BootLog.debug("left.kind=" + left.getKind());
            reg = fpuStack.getRegister(left);
            if (!commutative) {
                FPUHelper.fxch(os, fpuStack, reg);
            }
        } else {
            // Right operand is on FPU stack, left is not
            left.pushToFPU(ec); // Now left is on top
            reg = fpuStack.getRegister(right);
        }

        return reg;
    }

    /**
     * frem / drem
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void rem(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 % fpv2));
            v1.release(ec);
            v2.release(ec);
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            // We need reg to be ST1, if not swap
            fxchST1(os, fpuStack, reg);

            // Pop the fpuStack.tos
            fpuStack.pop();

            // Calculate
            os.writeFXCH(X86Register.ST1);
            os.writeFPREM();
            os.writeFSTP(X86Register.ST1);

            // Push result
            vstack.push(fpuStack.tos());
        }
    }

    /**
     * fsub / dsub
     *
     * @param ec
     * @param vstack
     * @param type
     */
    final void sub(int type) {
        final ItemFactory ifac = ec.getItemFactory();
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(ifac, type, fpv1 - fpv2));
            v1.release(ec);
            v2.release(ec);
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final FPU reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFSUBP(reg);

            // Push result
            vstack.push(result);
        }
    }

    /**
     * faload / daload
     *
     * @param type
     */
    final void fpaload(int type) {
        final IntItem idx = vstack.popInt();
        final RefItem ref = vstack.popRef();

        idx.loadIf(ec, ~Item.Kind.CONSTANT);
        ref.load(ec);
        final GPR refr = ref.getRegister();

        bcv.checkBounds(ref, idx);
        ensureStackCapacity(os, ec, vstack, 1);

        if (type == JvmType.FLOAT) {
            if (idx.isConstant()) {
                final int offset = idx.getValue() * 4;
                os.writeFLD32(refr, offset + arrayDataOffset);
            } else {
                GPR idxr = idx.getRegister();
                if (os.isCode64()) {
                    final GPR64 idxr64 = (GPR64) ec.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
                    os.writeMOVSXD(idxr64, (GPR32) idxr);
                    idxr = idxr64;
                }
                os.writeFLD32(refr, idxr, 4, arrayDataOffset);
            }
        } else {
            if (idx.isConstant()) {
                final int offset = idx.getValue() * 8;
                os.writeFLD64(refr, offset + arrayDataOffset);
            } else {
                GPR idxr = idx.getRegister();
                if (os.isCode64()) {
                    final GPR64 idxr64 = (GPR64) ec.getGPRPool().getRegisterInSameGroup(idxr, JvmType.LONG);
                    os.writeMOVSXD(idxr64, (GPR32) idxr);
                    idxr = idxr64;
                }
                os.writeFLD64(refr, idxr, 8, arrayDataOffset);
            }
        }

        // Release
        ref.release(ec);
        idx.release(ec);

        // Push result
        final ItemFactory ifac = ec.getItemFactory();
        final Item result = ifac.createFPUStack(type);
        vstack.fpuStack.push(result);
        vstack.push(result);
    }
}
