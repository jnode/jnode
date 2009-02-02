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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.Vm;

/**
 * This class is the base of all virtual stack items. To improve performance and
 * avoid type casts, all accessor methods are defined here (as abstract),
 * subclasses shall only implement those that make sense to them and throw an
 * exception otherwise (i.e. a reference item cannot implement load64 in any
 * meaningful way). The compiler knows about the various types and must access
 * only the legal methods.
 *
 * @author Patrik Reali
 *         <p/>
 *         TODO: make Item a subclass of operand
 */
abstract class Item {

    /**
     * Description of the virtual stack entry kind;
     * <p/>
     * An item has only one kind; flags are used for masking purposes (detect
     * multiple kinds in one operation)
     */
    static class Kind {

        /**
         * Item is constant
         */
        static final byte CONSTANT = 0x01;

        /**
         * Item is in a general purpose register
         */
        static final byte GPR = 0x02;

        /**
         * Item is in a SSE register
         */
        static final byte XMM = 0x04;

        /**
         * Item is on the FPU stack
         */
        static final byte FPUSTACK = 0x08;

        /**
         * Item is on the stack
         */
        static final byte STACK = 0x10;

        /**
         * Item is a local variable (EBP relative)
         */
        static final byte LOCAL = 0x20;

        public static final String toString(int kind) {
            switch (kind) {
                case STACK:
                    return "STACK";
                case GPR:
                    return "GPR";
                case XMM:
                    return "XMM";
                case FPUSTACK:
                    return "FPU";
                case LOCAL:
                    return "LOCAL";
                case CONSTANT:
                    return "CONST";
                default:
                    return String.valueOf(kind);
            }
        }
    }

    /*
      * Virtual Stack Item
      */
    private byte kind; // entry kind

    /**
     * Only valid for (kind == local)
     */
    private short offsetToFP;

    /**
     * Only valid for (kind == xmm)
     */
    protected X86Register.XMM xmm;

    /**
     * The factory that created me
     */
    protected final ItemFactory factory;

    /**
     * Initialize a blank item.
     */
    protected Item(ItemFactory factory) {
        this.factory = factory;
        if (false) {
            final Vm vm = Vm.getVm();
            final String name = getClass().getName();
            vm.getCounterGroup(name).getCounter("new").inc();
        }
    }

    /**
     * Initialize this instance.
     *
     * @param kind
     * @param offsetToFP
     * @param xmm
     */
    protected final void initialize(byte kind, short offsetToFP, X86Register.XMM xmm) {
        if (Vm.VerifyAssertions) Vm._assert(kind > 0, "Invalid kind");
        this.kind = kind;
        this.offsetToFP = offsetToFP;
        this.xmm = xmm;
    }

    /**
     * Throw a not implemented error.
     */
    static void notImplemented() {
        throw new Error("NotImplemented");
    }

    /**
     * Get the JVM type of this item
     *
     * @return the JVM type
     */
    abstract int getType();

    /**
     * Get the item kind (STACK, GPR, ....)
     *
     * @return the item kind
     */
    final byte getKind() {
        return kind;
    }

    /**
     * Gets the weight of the item kind (STACK, GPR, ....).
     * Kind weight is sorted from constant, gpr, xmm, fp, stack to local.
     *
     * @return the item kind weight
     */
    final int getKindWeight() {
        return kind;
    }

    /**
     * Sets the kind of this item.
     *
     * @param kind
     */
    protected final void setKind(byte kind) {
        this.kind = kind;
    }

    /**
     * Is this item on the stack
     */
    final boolean isStack() {
        return (kind == Kind.STACK);
    }

    /**
     * Is this item in a general purpose register
     */
    final boolean isGPR() {
        return (kind == Kind.GPR);
    }

    /**
     * Is this item in a SSE register
     */
    final boolean isXMM() {
        return (kind == Kind.XMM);
    }

    /**
     * Is this item on the FPU stack
     */
    final boolean isFPUStack() {
        return (kind == Kind.FPUSTACK);
    }

    /**
     * Is this item a local variable
     */
    final boolean isLocal() {
        return (kind == Kind.LOCAL);
    }

    /**
     * Is this item a constant
     */
    final boolean isConstant() {
        return (kind == Kind.CONSTANT);
    }

    /**
     * Return the current item's computational type category (JVM Spec, p. 83).
     * In practice, this is the number of double words needed by the item (1 or
     * 2)
     *
     * @return computational type category
     */
    int getCategory() {
        return 1;
    }

    /**
     * Gets the offset from this item to the FramePointer register. This is only
     * valid if this item has a LOCAL kind.
     *
     * @return
     */
    short getOffsetToFP(EmitterContext ec) {
        if (Vm.VerifyAssertions) {
            Vm._assert(isLocal(), "kind == Kind.LOCAL");
        }
        return offsetToFP;
    }

    /**
     * Gets the xmm register containing this item.
     * This is only valid if this item has a XMM kind.
     *
     * @return
     */
    final X86Register.XMM getXMM() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isXMM(), "kind == Kind.XMM");
        }
        return xmm;
    }

    /**
     * Is this item located at the given FP offset.
     *
     * @return
     */
    boolean isAtOffset(int offset) {
        if (Vm.VerifyAssertions) Vm._assert(kind == Kind.LOCAL, "kind == Kind.LOCAL");
        return (offsetToFP == offset);
    }

    /**
     * Load item into a register / two registers / an FPU register depending on
     * its type.
     *
     * @param ec the EmitterContext
     */
    abstract void load(EmitterContext ec);

    /**
     * Load this item to a general purpose register(s).
     *
     * @param ec
     */
    abstract void loadToGPR(EmitterContext ec);

    /**
     * Load this item to an XMM register.
     *
     * @param ec
     */
    abstract void loadToXMM(EmitterContext ec);

    /**
     * Load item into a register / two registers / an FPU register depending on
     * its type, if its kind matches the mask
     */
    final void loadIf(EmitterContext eContext, int mask) {
        if ((kind & mask) > 0) {
            load(eContext);
        }
    }

    /**
     * Load item into an XMM register if its kind matches the mask
     */
    final void loadToXMMIf(EmitterContext eContext, int mask) {
        if ((kind & mask) > 0) {
            loadToXMM(eContext);
        }
    }

    /**
     * Clone item
     *
     * @param ec the EmitterContext
     * @return a clone of the item
     */
    abstract Item clone(EmitterContext ec);

    /**
     * Push item onto the stack
     *
     * @param ec the EmitterContext
     */
    abstract void push(EmitterContext ec);

    /**
     * Push the value of this item on the FPU stack. The item itself is not
     * changed in any way.
     */
    abstract void pushToFPU(EmitterContext ec);

    /**
     * Release the registers associated to this item
     *
     * @param ec the EmitterContext
     */
    abstract void release(EmitterContext ec);

    /**
     * Is this obsolete?
     *
     * @param ec
     */
    final void release1(EmitterContext ec) {
        if (VirtualStack.checkOperandStack) {
            VirtualStack vs = ec.getVStack();
            vs.operandStack.pop(this);
        }
        release(ec);
    }

    /**
     * Spill the registers associated to this item
     *
     * @param ec  the EmitterContext
     * @param reg the register to be spilled
     */
    abstract void spill(EmitterContext ec, X86Register reg);

    /**
     * Spill this item if it uses the given register.
     */
    final void spillIfUsing(EmitterContext ec, X86Register reg) {
        if (uses(reg)) {
            spill(ec, reg);
        }
    }

    /**
     * enquire whether the item uses this register
     *
     * @param reg
     * @return true, when reg is used by this item
     */
    abstract boolean uses(X86Register reg);

    /**
     * enquire whether the item uses a volatile register
     *
     * @param reg
     * @return true, when this item uses a volatile register.
     */
    abstract boolean usesVolatileRegister(X86RegisterPool pool);

    /**
     * Verify the consistency of the state of this item.
     * Throw an exception is the state is inconsistent.
     */
    protected abstract void verifyState(EmitterContext ec);

    public String toString() {
        return getType() + "," + getKind() + " ("
            + System.identityHashCode(this) + ")";
    }
}
