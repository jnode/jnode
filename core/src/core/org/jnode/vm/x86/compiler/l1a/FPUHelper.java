/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.system.BootLog;
import org.jnode.vm.JvmType;
import org.jnode.vm.bytecode.StackException;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPUHelper implements X86CompilerConstants {

    /**
     * fadd / dadd
     * 
     * @param ec
     * @param vstack
     * @param type
     */
    final static void add(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(type, fpv1 + fpv2));
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final Register reg;
            reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, true);
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
    final static void compare(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, boolean gt, int type, Label curInstrLabel) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        // Prepare operands
        final FPUStack fpuStack = vstack.fpuStack;
        final Register reg;
        reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
        // We need reg to be ST1.
        fxchST1(os, fpuStack, reg);

        final X86RegisterPool pool = ec.getPool();
        pool.request(EAX);
        final Register resr = pool.request(JvmType.INT);

        if (gt) {
            // Reverse order
            fxch(os, fpuStack, Register.ST1);
        }
        os.writeFUCOMPP(); // Compare, Pop twice
        os.writeFNSTSW_AX(); // Store fp status word in AX
        os.writeSAHF(); // Store AH to Flags
        
        // Pop fpu stack twice (FUCOMPP)
        fpuStack.pop();
        fpuStack.pop();
        
        final Label eqLabel = new Label(curInstrLabel + "eq");
        final Label ltLabel = new Label(curInstrLabel + "lt");
        final Label endLabel = new Label(curInstrLabel + "end");
        os.writeJCC(eqLabel, X86Constants.JE);
        os.writeJCC(ltLabel, X86Constants.JB);
        // Greater
        if (gt) {
            os.writeMOV_Const(resr, -1);
        } else {
            os.writeMOV_Const(resr, 1);
        }
        os.writeJMP(endLabel);
        // Equal
        os.setObjectRef(eqLabel);
        os.writeXOR(resr, resr);
        os.writeJMP(endLabel);
        // Less
        os.setObjectRef(ltLabel);
        if (gt) {
            os.writeMOV_Const(resr, 1);
        } else {
            os.writeMOV_Const(resr, -1);
        }
        // End
        os.setObjectRef(endLabel);

        // Push result
        final IntItem res = IntItem.createReg(resr);
        pool.transferOwnerTo(resr, res);
        vstack.push(res);

        // Release
        pool.release(EAX);
    }

    /**
     * f2x / d2x
     * 
     * @param ec
     * @param vstack
     */
    final static void convert(EmitterContext ec, VirtualStack vstack,
            int fromType, int toType) {
        final Item v = vstack.pop(fromType);
        if (v.isConstant()) {
            vstack.push(createConst(toType, getFPValue(v)));
        } else {
            v.pushToFPU(ec);
            vstack.fpuStack.pop(v);
            final Item result = createFPUStack(toType);
            vstack.push(result);
            vstack.fpuStack.push(result);
        }
    }

    /**
     * fdiv / ddiv
     * 
     * @param ec
     * @param vstack
     * @param type
     */
    final static void div(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(type, fpv1 / fpv2));
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final Register reg;
            reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFDIVP(reg);

            // Push result
            vstack.push(result);
        }
    }

    /**
     * frem / drem
     * 
     * @param ec
     * @param vstack
     * @param type
     */
    final static void rem(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(type, fpv1 / fpv2));
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final Register reg;
            reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            // We need reg to be ST1, if not swap
            fxchST1(os, fpuStack, reg);
            
            // Pop the fpuStack.tos
            fpuStack.pop();

            // Calculate
            os.writeFPREM();
            os.writeFXCH(Register.ST1);
            os.writeFFREEP(Register.ST0);

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
    final static void sub(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(type, fpv1 - fpv2));
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final Register reg;
            reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, false);
            final Item result = fpuStack.getItem(reg);
            fpuStack.pop();

            // Calculate
            os.writeFSUBP(reg);

            // Push result
            vstack.push(result);
        }
    }

    /**
     * Create a constant item with a floating point value. Constant value will
     * be converted to the given type first.
     * 
     * @param type
     * @param value
     * @return
     */
    private final static Item createConst(int type, double value) {
        switch (type) {
        case JvmType.DOUBLE:
            return DoubleItem.createConst(value);
        case JvmType.FLOAT:
            return FloatItem.createConst((float) value);
        case JvmType.INT:
            return IntItem.createConst((int) value);
        case JvmType.LONG:
            return LongItem.createConst((long) value);
        default:
            throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    /**
     * Create an item on the top of the FPU stack.
     * 
     * @param type
     * @return
     */
    private final static Item createFPUStack(int type) {
        switch (type) {
        case JvmType.DOUBLE:
            return DoubleItem.createFPUStack();
        case JvmType.FLOAT:
            return FloatItem.createFPUStack();
        case JvmType.INT:
            return IntItem.createFPUStack();
        case JvmType.LONG:
            return LongItem.createFPUStack();
        default:
            throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    /**
     * fmul / dmul
     * 
     * @param ec
     * @param vstack
     * @param type
     */
    final static void mul(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v2 = vstack.pop(type);
        final Item v1 = vstack.pop(type);

        if (v1.isConstant() && v2.isConstant()) {
            final double fpv1 = getFPValue(v1);
            final double fpv2 = getFPValue(v2);
            vstack.push(createConst(type, fpv1 * fpv2));
        } else {
            // Prepare stack
            final FPUStack fpuStack = vstack.fpuStack;
            final Register reg;
            reg = prepareForOperation(os, ec, vstack, fpuStack, v2, v1, true);
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
    final static void neg(AbstractX86Stream os, EmitterContext ec,
            VirtualStack vstack, int type) {
        final Item v = vstack.pop(type);
        if (v.isConstant()) {
            final double fpv = getFPValue(v);
            vstack.push(createConst(type, -fpv));
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
    private final static void prepareForOperation(AbstractX86Stream os,
            EmitterContext ec, VirtualStack vstack, FPUStack fpuStack, Item left) {
        final boolean onFpu = left.isFPUStack();

        // If the FPU stack will be full in this operation, we flush the vstack first.
        int extraItems = onFpu ? 0 : 1;
        ensureStackCapacity(os, ec, vstack, extraItems);
        
        if (onFpu) {
            // Operand is on the FPU stack
            if (!fpuStack.isTos(left)) {
                // operand not on top, exchange it.
                final Register reg = fpuStack.getRegister(left);
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
     * 
     * The item at ST0 is popped of the given fpuStack stack.
     * 
     * @param left
     * @param right
     */
    private final static Register prepareForOperation(AbstractX86Stream os,
            EmitterContext ec, VirtualStack vstack, FPUStack fpuStack,
            Item left, Item right, boolean commutative) {
        final boolean lOnFpu = left.isFPUStack();
        final boolean rOnFpu = right.isFPUStack();
        final Register reg;
        
        // If the FPU stack will be full in this operation, we flush the vstack first.
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
                    //System.out.println("stack: " + fpuStack + "\nleft:  " + left + "\nright: " + right + "\nreg:   " + reg);
                    fxch(os, fpuStack, reg);
                }
            } else {
                // Neither left not right on top
                fxch(os, fpuStack, fpuStack.getRegister(left)); // Swap left &
                                                                // ST0, now left
                                                                // is on top
                reg = fpuStack.getRegister(right);
            }
        } else if (!(lOnFpu || rOnFpu)) {
            // Neither operands are on the FPU stack
            left.pushToFPU(ec); 
            right.pushToFPU(ec); // Now right is on top
            reg = Register.ST1; // Left is just below top
            if (!commutative) {
                fxch(os, fpuStack, reg);                
            }
        } else if (lOnFpu) {
            // Left operand is on FPU stack, right is not
            right.pushToFPU(ec); // Now right is on top
            //BootLog.debug("left.kind=" + left.getKind());
            reg = fpuStack.getRegister(left);
            if (!commutative) {
                fxch(os, fpuStack, reg);
            }
        } else {
            // Right operand is on FPU stack, left is not
            left.pushToFPU(ec); // Now left is on top
            reg = fpuStack.getRegister(right);
        }

        return reg;
    }

    /**
     * Swap ST0 and fpuReg. Action is emitted to code & performed on fpuStack.
     * 
     * @param os
     * @param fpuStack
     * @param fpuReg
     */
    static final void fxch(AbstractX86Stream os, FPUStack fpuStack,
            Register fpuReg) {
        if (fpuReg == Register.ST0) {
            throw new StackException("Cannot fxch ST0");
        }
        os.writeFXCH(fpuReg);
        fpuStack.fxch(fpuReg);
    }
    
    /**
     * Ensure that there are at least items registers left on the FPU stack.
     * If the number of items is not free, the stack is flushed onto the CPU stack.
     * @param os
     * @param ec
     * @param vstack
     * @param items
     */
    static final void ensureStackCapacity(AbstractX86Stream os, EmitterContext ec, VirtualStack vstack, int items) {
    	final FPUStack fpuStack = vstack.fpuStack;
    	if (!fpuStack.hasCapacity(items)) {
        	BootLog.debug("Flush FPU stack;\n  fpuStack=" + fpuStack + ",\n  vstack  =" + vstack);
        	vstack.push(ec);
        	Item.assertCondition(fpuStack.hasCapacity(items), "Out of FPU stack");
    	}
    }
    
    /**
     * Swap the given register and ST1 unless the given register is already 
     * ST1.
     * @param os
     * @param fpuStack
     * @param fpuReg
     */
    private static final void fxchST1(AbstractX86Stream os, FPUStack fpuStack,
            Register fpuReg) {
        // We need reg to be ST1, if not swap
        if (fpuReg != Register.ST1) {
            // Swap reg with ST0
            fxch(os, fpuStack, fpuReg);
            fxch(os, fpuStack, Register.ST1);
            fxch(os, fpuStack, fpuReg);
        }
    }
    
    private static double getFPValue(Item item) {
    	switch (item.getType()) {
    	case JvmType.INT: return ((IntItem)item).getValue();
    	case JvmType.LONG: return ((LongItem)item).getValue();
    	case JvmType.FLOAT: return ((FloatItem)item).getValue();
    	case JvmType.DOUBLE: return ((DoubleItem)item).getValue();
    	default: throw new InternalError(" Cannot get FP value of item with type " + item.getType());
    	}
    }

}