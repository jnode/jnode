/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class DoubleWordItem extends Item implements
        X86CompilerConstants {

    private Register lsb;

    private Register msb;

    /**
     * @param kind
     * @param offsetToFP
     * @param lsb
     * @param msb
     */
    protected DoubleWordItem(int kind, int offsetToFP, Register lsb,
            Register msb) {
        super(kind, offsetToFP);
        this.lsb = lsb;
        this.msb = msb;
    }

    /**
     * Return the current item's computational type category (JVM Spec, p. 83).
     * In practice, this is the number of double words needed by the item (1 or
     * 2)
     * 
     * @return computational type category
     */
    final int getCategory() {
        return 2;
    }

    /**
     * Gets the register holding the LSB part of this item
     * 
     * @return
     */
    final Register getLsbRegister() {
        assertCondition(kind == Kind.REGISTER, "kind == Kind.REGISTER");
        return lsb;
    }

    /**
     * Gets the register holding the MSB part of this item
     * 
     * @return
     */
    final Register getMsbRegister() {
        assertCondition(kind == Kind.REGISTER, "kind == Kind.REGISTER");
        return msb;
    }

    /**
     * Gets the offset from this item to the FramePointer register. This is only
     * valid if this item has a LOCAL kind.
     * 
     * @return @deprecated Use {@link #getLsbOffsetToFP()}or
     *         {@link #getMsbOffsetToFP()}instead.
     */
    final int getOffsetToFP() {
        throw new Error("Do not use this");
    }

    /**
     * Gets the offset from the LSB part of this item to the FramePointer
     * register. This is only valid if this item has a LOCAL kind.
     * 
     * @return
     */
    final int getLsbOffsetToFP() {
        return super.getOffsetToFP();
    }

    /**
     * Gets the offset from the MSB part of this item to the FramePointer
     * register. This is only valid if this item has a LOCAL kind.
     * 
     * @return
     */
    final int getMsbOffsetToFP() {
        return super.getOffsetToFP() + 4;
    }

    /**
     * ?
     * 
     * @param ec
     * @param lsb
     * @param msb
     */
    final void loadTo(EmitterContext ec, Register lsb, Register msb) {
        AbstractX86Stream os = ec.getStream();
        X86RegisterPool pool = ec.getPool();
        //os.log("LongItem.log called "+Integer.toString(kind));
        assertCondition(lsb != msb, "lsb != msb");
        assertCondition(lsb != null, "lsb != null");
        assertCondition(msb != null, "msb != null");
        switch (kind) {
        case Kind.REGISTER:
            // invariant: (msb != lsb) && (this.msb != this.lsb)
            if (msb != this.lsb) {
                // generic case; avoid only if msb is lsb' (value overwriting)
                // invariant: (msb != this.lsb) && (msb != lsb) && (this.msb !=
                // this.lsb)
                // msb <- msb'
                // lsb <- lsb'
                if (msb != this.msb) {
                    os.writeMOV(INTSIZE, msb, this.msb);
                    if (lsb != this.msb) {
                        pool.release(this.msb);
                    }
                }
                if (lsb != this.lsb) {
                    // invariant: (msb != this.lsb) && (lsb != this.lsb) && (msb
                    // != lsb) && (this.msb != this.lsb)
                    os.writeMOV(INTSIZE, lsb, this.lsb);
                    //if (msb != this.lsb) { <- enforced by first if()
                    pool.release(this.lsb);
                    //}
                }
            } else if (lsb != this.msb) {
                // generic case, assignment sequence inverted; avoid only if lsb
                // is msb' (overwriting)
                // invariant: (msb == this.lsb) && (lsb != this.msb)
                // lsb <- lsb'
                // msb <- msb'
                // if (lsb != this.lsb) { <- always true, because msb ==
                // this.lsb
                os.writeMOV(INTSIZE, lsb, this.lsb);
                //	if (msb != this.lsb) { <- always false, because of invariant
                //		pool.release(this.lsb);
                //	}
                // }
                // if (msb != this.msb) { <- always true, because of invariant
                os.writeMOV(INTSIZE, msb, this.msb);
                //	if (lsb != this.msb) { <- always true, because of invariant
                pool.release(this.msb);
                //	}
                // }
            } else {
                // invariant: (msb == this.lsb) && (lsb == this.msb)
                // swap registers
                //TODO: handle allocation failure
                Register reg = pool.request(JvmType.INT);
                os.writeMOV(INTSIZE, reg, this.lsb);
                os.writeMOV(INTSIZE, this.lsb, this.msb);
                os.writeMOV(INTSIZE, this.msb, reg);
                pool.release(reg);
            }
            break;

        case Kind.LOCAL:
            os.writeMOV(INTSIZE, lsb, FP, offsetToFP);
            os.writeMOV(INTSIZE, msb, FP, offsetToFP + 4);
            break;

        case Kind.CONSTANT:
            loadToConstant(ec, os, lsb, msb);
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            if (VirtualStack.checkOperandStack) {
                final VirtualStack stack = ec.getVStack();
                stack.operandStack.pop(this);
            }
            os.writePOP(lsb);
            os.writePOP(msb);
            break;

        }
        kind = Kind.REGISTER;
        this.lsb = lsb;
        this.msb = msb;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param lsb
     * @param msb
     */
    protected abstract void loadToConstant(EmitterContext ec,
            AbstractX86Stream os, Register lsb, Register msb);

    /**
     * Load this item to a general purpose register tuple.
     * 
     * @param ec
     */
    final void loadToGPR(EmitterContext ec) {
        if (kind != Kind.REGISTER) {
            Register lsb = ec.getPool().request(JvmType.INT);
            if (lsb == null) {
                ec.getVStack().push(ec);
                lsb = ec.getPool().request(JvmType.INT);
            }
            assertCondition(lsb != null, "lsb != null");
            Register msb = ec.getPool().request(JvmType.INT);
            if (msb == null) {
                ec.getVStack().push(ec);
                msb = ec.getPool().request(JvmType.INT);
            }
            assertCondition(msb != null, "msb != null");
            loadTo(ec, lsb, msb);
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
     */
    final void load(EmitterContext ec) {
        if (kind != Kind.REGISTER) {
            X86RegisterPool pool = ec.getPool();

            Register l = pool.request(JvmType.INT, this);
            if (l == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                l = pool.request(getType(), this);
            }
            Register r = pool.request(JvmType.INT, this);
            if (r == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                r = pool.request(getType(), this);
            }
            assertCondition(r != null, "r != null");
            assertCondition(l != null, "l != null");
            loadTo(ec, l, r);
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
     */
    final void push(EmitterContext ec) {
        final AbstractX86Stream os = ec.getStream();
        //os.log("LongItem.push "+Integer.toString(getKind()));
        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            break;

        case Kind.LOCAL:
            os.writePUSH(FP, getLsbOffsetToFP());
            os.writePUSH(FP, getMsbOffsetToFP());
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            //nothing to do
            if (VirtualStack.checkOperandStack) {
                final VirtualStack stack = ec.getVStack();

                if (kind == Kind.STACK) {
                    // the item is not really pushed and popped
                    // but this checks that it is really the top
                    // element
                    stack.operandStack.pop(this);
                }
            }
            break;
        }

        release(ec);
        kind = Kind.STACK;

        if (VirtualStack.checkOperandStack) {
            final VirtualStack stack = ec.getVStack();
            stack.operandStack.push(this);

        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#pushToFPU(EmitterContext)
     */
    final void pushToFPU(EmitterContext ec) {
        notImplemented();

        final AbstractX86Stream os = ec.getStream();
        final VirtualStack stack = ec.getVStack();

        //os.log("LongItem.push "+Integer.toString(getKind()));
        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            os.writeFLD64(SP, 0);
            os.writeLEA(SP, SP, 8);
            break;

        case Kind.LOCAL:
            os.writeFLD64(FP, getLsbOffsetToFP());
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            os.writeFLD64(SP, 0);
            os.writeLEA(SP, SP, 8);
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            if (VirtualStack.checkOperandStack) {
                stack.operandStack.pop(this);
            }
            os.writeFLD64(SP, 0);
            os.writeLEA(SP, SP, 8);            
            break;
        }

        release(ec);
        kind = Kind.FPUSTACK;
        stack.fpuStack.push(this);
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected abstract void pushConstant(EmitterContext ec, AbstractX86Stream os);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#release(EmitterContext)
     */
    final void release(EmitterContext ec) {
        final X86RegisterPool pool = ec.getPool();

        switch (getKind()) {
        case Kind.REGISTER:
            pool.release(lsb);
            pool.release(msb);
            break;

        case Kind.LOCAL:
            // nothing to do
            break;

        case Kind.CONSTANT:
            // nothing to do
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            // nothing to do
            break;
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
     */
    final void spill(EmitterContext ec, Register reg) {
        notImplemented();
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
     */
    final boolean uses(Register reg) {
        return ((kind == Kind.REGISTER) && (msb.equals(reg) || lsb.equals(reg)));
    }
    

    /**
     * Create a WordItem in a register.
     * @param jvmType LONG, DOUBLE
     * @param lsb
     * @param msb
     * @return
     */
    static final DoubleWordItem createReg(int jvmType, Register lsb, Register msb) {
        switch (jvmType) {
        case JvmType.LONG:
            return LongItem.createReg(lsb, msb);
        case JvmType.DOUBLE:
            return DoubleItem.createReg(lsb, msb);
        default:
            throw new IllegalArgumentException("Invalid type " + jvmType);
        }
    }
}