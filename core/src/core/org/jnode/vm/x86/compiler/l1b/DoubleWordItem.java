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

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class DoubleWordItem extends Item implements
    X86CompilerConstants {

    /**
     * LSB Register in 32-bit mode
     */
    private X86Register.GPR32 lsb;

    /**
     * MSB Register in 32-bit mode
     */
    private X86Register.GPR32 msb;

    /**
     * Register in 64-bit mode
     */
    private X86Register.GPR64 reg;

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
    protected final void initialize(EmitterContext ec, byte kind, short offsetToFP,
                                    X86Register.GPR lsb, X86Register.GPR msb, X86Register.GPR64 reg,
                                    X86Register.XMM xmm) {
        super.initialize(kind, offsetToFP, xmm);
        this.lsb = (GPR32) lsb;
        this.msb = (GPR32) msb;
        this.reg = reg;
        if (Vm.VerifyAssertions) {
            if (isGPR()) {
                Vm._assert(((lsb != null) && (msb != null)) || (reg != null));
            }
            verifyState(ec);
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    protected final Item clone(EmitterContext ec) {
        final DoubleWordItem res;
        final X86Assembler os = ec.getStream();

        switch (getKind()) {
            case Kind.GPR:
                res = L1AHelper.requestDoubleWordRegisters(ec, getType());
                if (os.isCode32()) {
                    final GPR lsb = res.getLsbRegister(ec);
                    final GPR msb = res.getMsbRegister(ec);
                    os.writeMOV(INTSIZE, lsb, this.lsb);
                    os.writeMOV(INTSIZE, msb, this.msb);
                } else {
                    final GPR64 reg = res.getRegister(ec);
                    os.writeMOV(BITS64, reg, this.reg);
                }
                break;

            case Kind.LOCAL:
                res = (DoubleWordItem) factory.createLocal(getType(), super
                    .getOffsetToFP(ec));
                break;

            case Kind.CONSTANT:
                res = cloneConstant(ec);
                break;

            case Kind.FPUSTACK:
                // TODO
                notImplemented();
                res = null;
                break;

            case Kind.STACK:
                // TODO
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
    protected abstract DoubleWordItem cloneConstant(EmitterContext ec);

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
    final int getLsbOffsetToFP(EmitterContext ec) {
        return super.getOffsetToFP(ec);
    }

    /**
     * Gets the register holding the LSB part of this item in 32-bit mode.
     *
     * @return
     */
    final X86Register.GPR getLsbRegister(EmitterContext ec) {
        if (!ec.getStream().isCode32()) {
            throw new Error("Can only be used in 32-bit mode");
        }
        if (Vm.VerifyAssertions) {
            Vm._assert(isGPR(), "kind == Kind.REGISTER");
            Vm._assert(lsb != null, "lsb != null");
        }
        return lsb;
    }

    /**
     * Gets the offset from the MSB part of this item to the FramePointer
     * register. This is only valid if this item has a LOCAL kind.
     *
     * @return
     */
    final int getMsbOffsetToFP(EmitterContext ec) {
        return super.getOffsetToFP(ec) + 4;
    }

    /**
     * Gets the register holding the MSB part of this item in 32-bit mode.
     *
     * @return
     */
    final X86Register.GPR getMsbRegister(EmitterContext ec) {
        if (!ec.getStream().isCode32()) {
            throw new Error("Can only be used in 32-bit mode");
        }
        if (Vm.VerifyAssertions) {
            Vm._assert(isGPR(), "kind == Kind.GPR");
            Vm._assert(msb != null, "msb != null");
        }
        return msb;
    }

    /**
     * Gets the register holding this item in 64-bit mode.
     *
     * @return
     */
    final X86Register.GPR64 getRegister(EmitterContext ec) {
        if (!ec.getStream().isCode64()) {
            throw new Error("Can only be used in 64-bit mode");
        }
        if (Vm.VerifyAssertions) {
            Vm._assert(isGPR(), "kind == Kind.REGISTER");
            Vm._assert(reg != null, "reg != null reg=" + reg + " lsb=" + lsb
                + " msb=" + msb);
        }
        return reg;
    }

    /**
     * Gets the offset from this item to the FramePointer register. This is only
     * valid if this item has a LOCAL kind.
     *
     * @return In 32-bit mode, use {@link #getLsbOffsetToFP()}or
     *         {@link #getMsbOffsetToFP()}instead.
     */
    final short getOffsetToFP(EmitterContext ec) {
        if (ec.getStream().isCode32()) {
            throw new Error("Do not use this");
        } else {
            return super.getOffsetToFP(ec);
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
     */
    final void load(EmitterContext ec) {
        if (!isGPR()) {
            final X86RegisterPool pool = ec.getGPRPool();
            final X86Assembler os = ec.getStream();

            if (os.isCode32()) {
                X86Register.GPR l = (X86Register.GPR) pool.request(JvmType.INT,
                    this);
                if (l == null) {
                    final VirtualStack vstack = ec.getVStack();
                    vstack.push(ec);
                    l = (X86Register.GPR) pool.request(JvmType.INT, this);
                }
                X86Register.GPR r = (X86Register.GPR) pool.request(JvmType.INT,
                    this);
                if (r == null) {
                    final VirtualStack vstack = ec.getVStack();
                    vstack.push(ec);
                    r = (X86Register.GPR) pool.request(JvmType.INT, this);
                }
                if (Vm.VerifyAssertions) {
                    Vm._assert(r != null, "r != null");
                    Vm._assert(l != null, "l != null");
                }
                loadTo32(ec, l, r);
            } else {
                GPR64 r = (GPR64) pool.request(getType(), this);
                if (r == null) {
                    final VirtualStack vstack = ec.getVStack();
                    vstack.push(ec);
                    r = (GPR64) pool.request(getType(), this);
                }
                if (Vm.VerifyAssertions) {
                    Vm._assert(r != null, "r != null");
                }
                loadTo64(ec, r);
            }
        }
        if (Vm.VerifyAssertions) {
            verifyState(ec);
        }
    }

    /**
     * Load the value of this item into the given registers.
     *
     * @param ec
     * @param lsb
     * @param msb
     */
    final void loadTo32(EmitterContext ec, X86Register.GPR lsb,
                        X86Register.GPR msb) {
        final X86Assembler os = ec.getStream();
        final X86RegisterPool pool = ec.getGPRPool();
        final VirtualStack stack = ec.getVStack();
        if (!os.isCode32()) {
            throw new RuntimeException("Can only be used in 32-bit mode.");
        }

        // os.log("LongItem.log called "+Integer.toString(kind));
        if (Vm.VerifyAssertions) {
            Vm._assert(lsb != msb, "lsb != msb");
            Vm._assert(lsb != null, "lsb != null");
            Vm._assert(msb != null, "msb != null");
        }

        switch (getKind()) {
            case Kind.GPR:
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
                        // if (msb != this.lsb) { <- enforced by first if()
                        pool.release(this.lsb);
                        // }
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
                    // if (msb != this.lsb) { <- always false, because of invariant
                    // pool.release(this.lsb);
                    // }
                    // }
                    // if (msb != this.msb) { <- always true, because of invariant
                    os.writeMOV(INTSIZE, msb, this.msb);
                    // if (lsb != this.msb) { <- always true, because of invariant
                    pool.release(this.msb);
                    // }
                    // }
                } else {
                    // invariant: (msb == this.lsb) && (lsb == this.msb)
                    // swap registers
                    os.writeXCHG(this.lsb, this.msb);
                }
                break;

            case Kind.LOCAL:
                final int ofs = super.getOffsetToFP(ec);
                os.writeMOV(INTSIZE, lsb, X86Register.EBP, ofs);
                os.writeMOV(INTSIZE, msb, X86Register.EBP, ofs + 4);
                break;

            case Kind.CONSTANT:
                loadToConstant32(ec, os, (GPR32) lsb, (GPR32) msb);
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
        setKind(Kind.GPR);
        this.lsb = (GPR32) lsb;
        this.msb = (GPR32) msb;
    }

    /**
     * Load the value of this item into the given registers. Only valid in
     * 64-bit mode.
     *
     * @param ec
     * @param reg
     */
    final void loadTo64(EmitterContext ec, X86Register.GPR64 reg) {
        final X86Assembler os = ec.getStream();
//      final X86RegisterPool pool = ec.getGPRPool();
        final VirtualStack stack = ec.getVStack();
        if (!os.isCode64()) {
            throw new RuntimeException("Can only be used in 64-bit mode.");
        }

        if (Vm.VerifyAssertions) {
            Vm._assert(reg != null, "reg != null");
        }

        switch (getKind()) {
            case Kind.GPR:
                if (this.reg != reg) {
                    os.writeMOV(BITS64, reg, this.reg);
                    cleanup(ec);
                }
                break;

            case Kind.LOCAL:
                os.writeMOV(BITS64, reg, X86Register.RBP, getOffsetToFP(ec));
                break;

            case Kind.CONSTANT:
                loadToConstant64(ec, os, reg);
                break;

            case Kind.FPUSTACK:
                // Make sure this item is on top of the FPU stack
                stack.fpuStack.pop(this);
                // Convert & move to new space on normal stack
                os.writeLEA(X86Register.RSP, X86Register.RSP, -8);
                popFromFPU(os, X86Register.RSP, 0);
                os.writePOP(reg);
                break;

            case Kind.STACK:
                if (VirtualStack.checkOperandStack) {
                    stack.operandStack.pop(this);
                }
                os.writePOP(reg);
                os.writeLEA(X86Register.RSP, X86Register.RSP, 8); // garbage
                break;

        }
        setKind(Kind.GPR);
        this.reg = reg;
    }

    /**
     * Load my constant to the given os in 32-bit mode.
     *
     * @param os
     * @param lsb
     * @param msb
     */
    protected abstract void loadToConstant32(EmitterContext ec,
                                             X86Assembler os, GPR32 lsb, GPR32 msb);

    /**
     * Load my constant to the given os in 64-bit mode.
     *
     * @param os
     * @param reg
     */
    protected abstract void loadToConstant64(EmitterContext ec,
                                             X86Assembler os, GPR64 reg);

    /**
     * Load this item to a general purpose register tuple.
     *
     * @param ec
     */
    final void loadToGPR(EmitterContext ec) {
        if (!isGPR()) {
            final X86Assembler os = ec.getStream();

            if (os.isCode32()) {
                X86Register.GPR lsb = (X86Register.GPR) ec.getGPRPool()
                    .request(JvmType.INT);
                if (lsb == null) {
                    ec.getVStack().push(ec);
                    lsb = (X86Register.GPR) ec.getGPRPool()
                        .request(JvmType.INT);
                }
                if (Vm.VerifyAssertions)
                    Vm._assert(lsb != null, "lsb != null");
                X86Register.GPR msb = (X86Register.GPR) ec.getGPRPool()
                    .request(JvmType.INT);
                if (msb == null) {
                    ec.getVStack().push(ec);
                    msb = (X86Register.GPR) ec.getGPRPool()
                        .request(JvmType.INT);
                }
                if (Vm.VerifyAssertions)
                    Vm._assert(msb != null, "msb != null");
                loadTo32(ec, lsb, msb);
            } else {
                GPR64 r = (GPR64) ec.getGPRPool().request(getType());
                if (r == null) {
                    ec.getVStack().push(ec);
                    r = (GPR64) ec.getGPRPool().request(getType());
                }
                if (Vm.VerifyAssertions) {
                    Vm._assert(r != null, "r != null");
                }
                loadTo64(ec, r);
            }
        }
    }

    /**
     * Load this item to an XMM register.
     *
     * @param ec
     */
    final void loadToXMM(EmitterContext ec) {
        throw new Error("Not implemented yet");
    }

    /**
     * Pop the top of the FPU stack into the given memory location.
     *
     * @param os
     * @param reg
     * @param disp
     */
    protected abstract void popFromFPU(X86Assembler os, GPR reg, int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
     */
    final void push(EmitterContext ec) {
        final X86Assembler os = ec.getStream();
        final VirtualStack stack = ec.getVStack();
        // os.log("LongItem.push "+Integer.toString(getKind()));

        switch (getKind()) {
            case Kind.GPR:
                if (os.isCode32()) {
                    os.writePUSH(msb);
                    os.writePUSH(lsb);
                } else {
                    os.writeLEA(X86Register.RSP, X86Register.RSP, -8); // garbage
                    os.writePUSH(reg);
                }
                break;

            case Kind.LOCAL:
                if (os.isCode32()) {
                    os.writePUSH(X86Register.EBP, getMsbOffsetToFP(ec));
                    os.writePUSH(X86Register.EBP, getLsbOffsetToFP(ec));
                } else {
                    os.writeLEA(X86Register.RSP, X86Register.RSP, -8); // garbage
                    os.writePUSH(X86Register.RBP, getOffsetToFP(ec));
                }
                break;

            case Kind.CONSTANT:
                if (os.isCode64()) {
                    os.writeLEA(X86Register.RSP, X86Register.RSP, -8); // garbage
                }
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
                if (os.isCode32()) {
                    os.writeLEA(X86Register.ESP, X86Register.ESP, -8);
                    popFromFPU(os, X86Register.ESP, 0);
                } else {
                    os.writeLEA(X86Register.RSP, X86Register.RSP, -8); // garbage
                    os.writeLEA(X86Register.RSP, X86Register.RSP, -8); // Still 8
                    // bytes
                    popFromFPU(os, X86Register.RSP, 0);
                }
                break;

            case Kind.STACK:
                // nothing to do
                if (VirtualStack.checkOperandStack) {
                    // the item is not really pushed and popped
                    // but this checks that it is really the top
                    // element
                    stack.operandStack.pop(this);
                }
                break;
        }

        cleanup(ec);
        setKind(Kind.STACK);

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
    protected abstract void pushToFPU(X86Assembler os, GPR reg, int disp);

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#pushToFPU(EmitterContext)
     */
    final void pushToFPU(EmitterContext ec) {
        final X86Assembler os = ec.getStream();
        final VirtualStack stack = ec.getVStack();

        // os.log("LongItem.push "+Integer.toString(getKind()));
        switch (getKind()) {
            case Kind.GPR:
                if (os.isCode32()) {
                    os.writePUSH(msb);
                    os.writePUSH(lsb);
                    pushToFPU(os, X86Register.ESP, 0);
                    os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
                } else {
                    os.writePUSH(reg);
                    pushToFPU(os, X86Register.RSP, 0);
                    os.writeLEA(X86Register.RSP, X86Register.RSP, 8);
                }
                break;

            case Kind.LOCAL:
                if (os.isCode32()) {
                    pushToFPU(os, X86Register.EBP, getLsbOffsetToFP(ec));
                } else {
                    pushToFPU(os, X86Register.RBP, getOffsetToFP(ec));
                }
                break;

            case Kind.CONSTANT:
                pushConstant(ec, os);
                if (os.isCode32()) {
                    pushToFPU(os, X86Register.ESP, 0);
                    os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
                } else {
                    pushToFPU(os, X86Register.RSP, 0);
                    os.writeLEA(X86Register.RSP, X86Register.RSP, 8);
                }
                break;

            case Kind.FPUSTACK:
                // Assert this item is at the top of the stack
                stack.fpuStack.pop(this);
                stack.fpuStack.push(this);
                return;
                // break;

            case Kind.STACK:
                if (VirtualStack.checkOperandStack) {
                    stack.operandStack.pop(this);
                }
                if (os.isCode32()) {
                    pushToFPU(os, X86Register.ESP, 0);
                    os.writeLEA(X86Register.ESP, X86Register.ESP, 8);
                } else {
                    pushToFPU(os, X86Register.RSP, 0);
                    os.writeLEA(X86Register.RSP, X86Register.RSP, 16); // 8-byte + garbage
                }
                break;
        }

        cleanup(ec);
        setKind(Kind.FPUSTACK);
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
        // assertCondition(!ec.getVStack().contains(this), "Cannot release while
        // on vstack");
        final X86RegisterPool pool = ec.getGPRPool();
        final X86Assembler os = ec.getStream();

        switch (getKind()) {
            case Kind.GPR:
                if (os.isCode32()) {
                    pool.release(lsb);
                    pool.release(msb);
                } else {
                    pool.release(reg);
                }
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
        this.reg = null;
        setKind((byte) 0);
    }

    private final X86Register request(EmitterContext ec, X86RegisterPool pool) {
        final X86Assembler os = ec.getStream();
        final X86Register r;
        if (os.isCode32()) {
            r = pool.request(JvmType.INT);
        } else {
            r = pool.request(getType());
        }
        if (Vm.VerifyAssertions) {
            Vm._assert(r != null, "r != null");
        }
        return r;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
     */
    final void spill(EmitterContext ec, X86Register reg) {
        final X86Assembler os = ec.getStream();
        if (Vm.VerifyAssertions) {
            Vm._assert(getKind() == Kind.GPR);
            if (os.isCode32()) {
                Vm._assert((this.lsb == reg) || (this.msb == reg), "spill1");
            } else {
                Vm._assert((this.reg.getNr() == reg.getNr()), "spill1");
            }
        }
        ec.getVStack().push(ec);
        if (isStack()) {
            return;
        }

        final X86RegisterPool pool = ec.getGPRPool();
        if (os.isCode32()) {
            final X86Register.GPR newLsb = (X86Register.GPR) request(ec, pool);
            final X86Register.GPR newMsb = (X86Register.GPR) request(ec, pool);
            loadTo32(ec, newLsb, newMsb);
            pool.transferOwnerTo(newLsb, this);
            pool.transferOwnerTo(newMsb, this);
        } else {
            final GPR64 newReg = (GPR64) request(ec, pool);
            loadTo64(ec, newReg);
            pool.transferOwnerTo(newReg, this);
        }
        if (Vm.VerifyAssertions) {
            verifyState(ec);
        }
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
     */
    final boolean uses(X86Register reg) {
        return (isGPR() && ((this.msb == reg) || (this.lsb == reg) || (this.reg == reg)));
    }

    /**
     * enquire whether the item uses a volatile register
     *
     * @param reg
     * @return true, when this item uses a volatile register.
     */
    final boolean usesVolatileRegister(X86RegisterPool pool) {
        if (isGPR()) {
            if (reg == null) {
                // 32-bit
                return (!(pool.isCallerSaved(lsb) && pool.isCallerSaved(msb)));
            } else {
                // 64-bit
                return (!(pool.isCallerSaved(reg)));
            }
        }
        return false;
    }

    /**
     * Verify the consistency of the state of this item. Throw an exception is
     * the state is inconsistent.
     */
    protected void verifyState(EmitterContext ec) {
        switch (getKind()) {
            case Kind.GPR:
                if (ec.getStream().isCode32()) {
                    if (lsb == null) {
                        throw new IllegalStateException("lsb cannot be null");
                    }
                    if (msb == null) {
                        throw new IllegalStateException("msb cannot be null");
                    }
                    if (!(lsb instanceof GPR32)) {
                        throw new IllegalStateException("lsb must be GPR32");
                    }
                    if (!(msb instanceof GPR32)) {
                        throw new IllegalStateException("msb must be GPR32");
                    }
                } else {
                    if (reg == null) {
                        throw new IllegalStateException("reg cannot be null");
                    }
                    if (!(reg instanceof GPR64)) {
                        throw new IllegalStateException("reg must be GPR64");
                    }
                }
                break;
        }
    }
}
