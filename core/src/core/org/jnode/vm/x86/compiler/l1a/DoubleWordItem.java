/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class DoubleWordItem extends Item implements
        X86CompilerConstants {

    private X86Register lsb;

    private X86Register msb;

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
    protected final void initialize(int kind, int offsetToFP, X86Register lsb,
            X86Register msb) {
        super.initialize(kind, offsetToFP);
        this.lsb = lsb;
        this.msb = msb;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    protected final Item clone(EmitterContext ec) {
        final DoubleWordItem res;
        final X86Assembler os = ec.getStream();

        switch (getKind()) {
        case Kind.REGISTER:
            res = L1AHelper.requestDoubleWordRegisters(ec, getType());
            final X86Register lsb = res.getLsbRegister();
            final X86Register msb = res.getMsbRegister();
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
    final X86Register getLsbRegister() {
        if (Vm.VerifyAssertions) Vm._assert(kind == Kind.REGISTER, "kind == Kind.REGISTER");
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
    final X86Register getMsbRegister() {
        if (Vm.VerifyAssertions) Vm._assert(kind == Kind.REGISTER, "kind == Kind.REGISTER");
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

            X86Register l = pool.request(JvmType.INT, this);
            if (l == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                l = pool.request(JvmType.INT, this);
            }
            X86Register r = pool.request(JvmType.INT, this);
            if (r == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                r = pool.request(JvmType.INT, this);
            }
            if (Vm.VerifyAssertions) Vm._assert(r != null, "r != null");
            if (Vm.VerifyAssertions) Vm._assert(l != null, "l != null");
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
    final void loadTo(EmitterContext ec, X86Register lsb, X86Register msb) {
        final X86Assembler os = ec.getStream();
        final X86RegisterPool pool = ec.getPool();
        final VirtualStack stack = ec.getVStack();

        //os.log("LongItem.log called "+Integer.toString(kind));
        if (Vm.VerifyAssertions) { 
            Vm._assert(lsb != msb, "lsb != msb");
            Vm._assert(lsb != null, "lsb != null");
            Vm._assert(msb != null, "msb != null");
        }

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
            os.writeMOV(INTSIZE, lsb, X86Register.EBP, offsetToFP);
            os.writeMOV(INTSIZE, msb, X86Register.EBP, offsetToFP + 4);
            break;

        case Kind.CONSTANT:
            loadToConstant(ec, os, lsb, msb);
            break;

        case Kind.FPUSTACK:
            // Make sure this item is on top of the FPU stack
            stack.fpuStack.pop(this);
            // Convert & move to new space on normal stack
            os.writeLEA(X86Register.ESP, X86Register.ESP, -8);
            popFromFPU(os, X86Register.ESP, 0);
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
            X86Assembler os, X86Register lsb, X86Register msb);

    /**
     * Load this item to a general purpose register tuple.
     * 
     * @param ec
     */
    final void loadToGPR(EmitterContext ec) {
        if (kind != Kind.REGISTER) {
            X86Register lsb = ec.getPool().request(JvmType.INT);
            if (lsb == null) {
                ec.getVStack().push(ec);
                lsb = ec.getPool().request(JvmType.INT);
            }
            if (Vm.VerifyAssertions) Vm._assert(lsb != null, "lsb != null");
            X86Register msb = ec.getPool().request(JvmType.INT);
            if (msb == null) {
                ec.getVStack().push(ec);
                msb = ec.getPool().request(JvmType.INT);
            }
            if (Vm.VerifyAssertions) Vm._assert(msb != null, "msb != null");
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
    protected abstract void popFromFPU(X86Assembler os, X86Register reg,
            int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
     */
    final void push(EmitterContext ec) {
        final X86Assembler os = ec.getStream();
        final VirtualStack stack = ec.getVStack();
        //os.log("LongItem.push "+Integer.toString(getKind()));

        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            break;

        case Kind.LOCAL:
            os.writePUSH(X86Register.EBP, getMsbOffsetToFP());
            os.writePUSH(X86Register.EBP, getLsbOffsetToFP());
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
            os.writeLEA(X86Register.ESP, X86Register.ESP, -8);
            popFromFPU(os, X86Register.ESP, 0);
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
    protected abstract void pushConstant(EmitterContext ec, X86Assembler os);

    /**
     * Push the value at the given memory location on the FPU stack.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected abstract void pushToFPU(X86Assembler os, X86Register reg,
            int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#pushToFPU(EmitterContext)
     */
    final void pushToFPU(EmitterContext ec) {
        final X86Assembler os = ec.getStream();
        final VirtualStack stack = ec.getVStack();

        //os.log("LongItem.push "+Integer.toString(getKind()));
        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(msb);
            os.writePUSH(lsb);
            pushToFPU(os, X86Register.ESP, 0);
            os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
            break;

        case Kind.LOCAL:
            pushToFPU(os, X86Register.EBP, getLsbOffsetToFP());
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            pushToFPU(os, X86Register.ESP, 0);
            os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
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
            pushToFPU(os, X86Register.ESP, 0);
            os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
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

    private final X86Register request(EmitterContext ec, X86RegisterPool pool) {
        final X86Register r = pool.request(JvmType.INT);
        if (Vm.VerifyAssertions) Vm._assert(r != null, "r != null");
        return r;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
     */
    final void spill(EmitterContext ec, X86Register reg) {
        if (Vm.VerifyAssertions) Vm._assert((getKind() == Kind.REGISTER)
                && ((this.lsb == reg) || (this.msb == reg)), "spill1");
        ec.getVStack().push(ec);
        if (isStack()) {
            return;
        }

        final X86RegisterPool pool = ec.getPool();
        final X86Register newLsb = request(ec, pool);
        final X86Register newMsb = request(ec, pool);
        loadTo(ec, newLsb, newMsb);
        pool.transferOwnerTo(newLsb, this);
        pool.transferOwnerTo(newMsb, this);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
     */
    final boolean uses(X86Register reg) {
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
