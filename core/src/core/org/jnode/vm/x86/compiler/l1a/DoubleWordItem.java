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
     * Initialize a blank item.
     */
    protected DoubleWordItem(ItemFactory factory) {
        super(factory);
    }

    /**
     * @param kind
     * @param offsetToFP
     * @param lsb
     * @param msb
     */
    protected final void initialize(int kind, int offsetToFP, Register lsb,
            Register msb) {
        super.initialize(kind, offsetToFP);
        this.lsb = lsb;
        this.msb = msb;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    protected final Item clone(EmitterContext ec) {
        final DoubleWordItem res;
        final AbstractX86Stream os = ec.getStream();

        switch (getKind()) {
        case Kind.REGISTER:
            res = L1AHelper.requestDoubleWordRegisters(ec, getType());
            final Register lsb = res.getLsbRegister();
            final Register msb = res.getMsbRegister();
            os.writeMOV(INTSIZE, lsb, this.lsb);
            os.writeMOV(INTSIZE, msb, this.msb);
            break;

        case Kind.LOCAL:
            res = (DoubleWordItem)factory.createLocal(getType(), super.getOffsetToFP());
            break;

        case Kind.CONSTANT:
            res = cloneConstant();
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            res = null;
            break;
 
        case Kind.STACK:
            //TODO
            notImplemented();
            res = null;
            break;

        default:
            throw new IllegalArgumentException("Invalid item kind");
        }
        return res;
    }

    /**
     * Create a clone of this item, which must be a constant.
     * 
     * @return
     */
    protected abstract DoubleWordItem cloneConstant();

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
     * Gets the offset from the LSB part of this item to the FramePointer
     * register. This is only valid if this item has a LOCAL kind.
     * 
     * @return
     */
    final int getLsbOffsetToFP() {
        return super.getOffsetToFP();
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
     * Gets the offset from the MSB part of this item to the FramePointer
     * register. This is only valid if this item has a LOCAL kind.
     * 
     * @return
     */
    final int getMsbOffsetToFP() {
        return super.getOffsetToFP() + 4;
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
     * @return
     * @deprecated Use {@link #getLsbOffsetToFP()}or
     *             {@link #getMsbOffsetToFP()}instead.
     */
    final int getOffsetToFP() {
        throw new Error("Do not use this");
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
                l = pool.request(JvmType.INT, this);
            }
            Register r = pool.request(JvmType.INT, this);
            if (r == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                r = pool.request(JvmType.INT, this);
            }
            assertCondition(r != null, "r != null");
            assertCondition(l != null, "l != null");
            loadTo(ec, l, r);
        }
    }

    /**
     * ?
     * 
     * @param ec
     * @param lsb
     * @param msb
     */
    final void loadTo(EmitterContext ec, Register lsb, Register msb) {
        final AbstractX86Stream os = ec.getStream();
        final X86RegisterPool pool = ec.getPool();
        final VirtualStack stack = ec.getVStack();

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
                os.writeXCHG(this.lsb, this.msb);
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
            // Make sure this item is on top of the FPU stack
            stack.fpuStack.pop(this);
            // Convert & move to new space on normal stack
            os.writeLEA(SP, SP, -8);
            popFromFPU(os, SP, 0);
            os.writePOP(lsb);
            os.writePOP(msb);
            break;

        case Kind.STACK:
            if (VirtualStack.checkOperandStack) {
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
     * Pop the top of the FPU stack into the given memory location.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected abstract void popFromFPU(AbstractX86Stream os, Register reg,
            int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
     */
    final void push(EmitterContext ec) {
        final AbstractX86Stream os = ec.getStream();
        final VirtualStack stack = ec.getVStack();
        //os.log("LongItem.push "+Integer.toString(getKind()));

        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            break;

        case Kind.LOCAL:
            os.writePUSH(FP, getMsbOffsetToFP());
            os.writePUSH(FP, getLsbOffsetToFP());
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            break;

        case Kind.FPUSTACK:
            // Make sure this item is on top of the FPU stack
            final FPUStack fpuStack = stack.fpuStack;
            if (!fpuStack.isTos(this)) {
                FPUHelper.fxch(os, fpuStack, fpuStack.getRegister(this));
            }
            stack.fpuStack.pop(this);
            // Convert & move to new space on normal stack
            os.writeLEA(SP, SP, -8);
            popFromFPU(os, SP, 0);
            break;

        case Kind.STACK:
            //nothing to do
            if (VirtualStack.checkOperandStack) {
                // the item is not really pushed and popped
                // but this checks that it is really the top
                // element
                stack.operandStack.pop(this);
            }
            break;
        }

        cleanup(ec);
        kind = Kind.STACK;

        if (VirtualStack.checkOperandStack) {
            stack.operandStack.push(this);
        }
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected abstract void pushConstant(EmitterContext ec, AbstractX86Stream os);

    /**
     * Push the value at the given memory location on the FPU stack.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected abstract void pushToFPU(AbstractX86Stream os, Register reg,
            int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#pushToFPU(EmitterContext)
     */
    final void pushToFPU(EmitterContext ec) {
        final AbstractX86Stream os = ec.getStream();
        final VirtualStack stack = ec.getVStack();

        //os.log("LongItem.push "+Integer.toString(getKind()));
        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            pushToFPU(os, SP, 0);
            os.writeLEA(SP, SP, 8);
            break;

        case Kind.LOCAL:
            pushToFPU(os, FP, getLsbOffsetToFP());
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            pushToFPU(os, SP, 0);
            os.writeLEA(SP, SP, 8);
            break;

        case Kind.FPUSTACK:
            // Assert this item is at the top of the stack
            stack.fpuStack.pop(this);
            stack.fpuStack.push(this);
            return;
        //break;

        case Kind.STACK:
            if (VirtualStack.checkOperandStack) {
                stack.operandStack.pop(this);
            }
            pushToFPU(os, SP, 0);
            os.writeLEA(SP, SP, 8);
            break;
        }

        cleanup(ec);
        kind = Kind.FPUSTACK;
        stack.fpuStack.push(this);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#release(EmitterContext)
     */
    final void release(EmitterContext ec) {
        cleanup(ec);
        factory.release(this);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#release(EmitterContext)
     */
    private final void cleanup(EmitterContext ec) {
        //assertCondition(!ec.getVStack().contains(this), "Cannot release while
        // on vstack");
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
            // nothing to do
            break;

        case Kind.STACK:
            // nothing to do
            break;
        }

        this.lsb = null;
        this.msb = null;
        this.kind = 0;
    }

    private final Register request(EmitterContext ec, X86RegisterPool pool) {
        final Register r = pool.request(JvmType.INT);
        assertCondition(r != null, "r != null");
        return r;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
     */
    final void spill(EmitterContext ec, Register reg) {
        assertCondition((getKind() == Kind.REGISTER)
                && ((this.lsb == reg) || (this.msb == reg)), "spill1");
        ec.getVStack().push(ec);
        if (isStack()) {
            return;
        }

        final X86RegisterPool pool = ec.getPool();
        final Register newLsb = request(ec, pool);
        final Register newMsb = request(ec, pool);
        loadTo(ec, newLsb, newMsb);
        pool.transferOwnerTo(newLsb, this);
        pool.transferOwnerTo(newMsb, this);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
     */
    final boolean uses(Register reg) {
        return ((kind == Kind.REGISTER) && (msb.equals(reg) || lsb.equals(reg)));
    }

    /**
     * enquire whether the item uses a volatile register
     * 
     * @param reg
     * @return true, when this item uses a volatile register.
     */
    final boolean usesVolatileRegister(X86RegisterPool pool) {
        return ((kind == Kind.REGISTER) && !(pool.isCallerSaved(lsb) && pool
                .isCallerSaved(msb)));
    }
}