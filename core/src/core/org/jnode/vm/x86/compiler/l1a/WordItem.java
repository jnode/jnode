/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class WordItem extends Item implements X86CompilerConstants {

    private Register reg;

    protected WordItem(ItemFactory factory) {
        super(factory);
    }

    protected final void initialize(int kind, Register reg, int local) {
        super.initialize(kind, local);
        this.reg = reg;
        if (Vm.VerifyAssertions)
            Vm._assert((kind != Kind.REGISTER) || (reg != null),
                    "kind == register implies that reg != null");
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    protected Item clone(EmitterContext ec) {
        final WordItem res;
        final AbstractX86Stream os = ec.getStream();

        switch (getKind()) {
        case Kind.REGISTER:
            res = L1AHelper.requestWordRegister(ec, getType(), false);
            final Register r = res.getRegister();
            os.writeMOV(INTSIZE, r, reg);
            break;

        case Kind.LOCAL:
            res = (WordItem) factory.createLocal(getType(), getOffsetToFP());
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
            os.writePUSH(Register.SP, 0);
            res = (WordItem) factory.createStack(getType());
            if (VirtualStack.checkOperandStack) {
                final ItemStack operandStack = ec.getVStack().operandStack;
                operandStack.pop(this);
                operandStack.push(this);
                operandStack.push(res);
            }
            break;

        default:
            throw new IllegalArgumentException("Invalid item kind");
        }
        return res;
    }

    /**
     * Create a clone of this item, which must be a constant.
     * 
     * @return The cloned item
     */
    protected abstract WordItem cloneConstant();

    /**
     * Gets the register the is used by this item.
     * 
     * @return The register that contains this item
     */
    final Register getRegister() {
        if (Vm.VerifyAssertions)
            Vm._assert(kind == Kind.REGISTER, "Must be register");
        return reg;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
     */
    final void load(EmitterContext ec) {
        if (kind != Kind.REGISTER) {
            final X86RegisterPool pool = ec.getPool();
            Register r = pool.request(getType(), this);
            if (r == null) {
                final VirtualStack vstack = ec.getVStack();
                vstack.push(ec);
                r = pool.request(getType(), this);
            }
            if (Vm.VerifyAssertions)
                Vm._assert(r != null, "r != null");
            loadTo(ec, r);
        }
    }

    /**
     * load item with register reg. Assumes that reg is properly allocated
     * 
     * @param ec
     *            current emitter context
     * @param reg
     *            register to load the item to
     */
    final void loadTo(EmitterContext ec, Register reg) {
        if (Vm.VerifyAssertions)
            Vm._assert(reg != null, "Reg != null");
        final AbstractX86Stream os = ec.getStream();
        final X86RegisterPool pool = ec.getPool();
        final VirtualStack stack = ec.getVStack();
        if (Vm.VerifyAssertions)
            Vm._assert(!pool.isFree(reg), "reg not free");

        switch (kind) {
        case Kind.REGISTER:
            if (this.reg != reg) {
                os.writeMOV(INTSIZE, reg, this.reg);
                cleanup(ec);
            }
            break;

        case Kind.LOCAL:
            os.writeMOV(INTSIZE, reg, FP, getOffsetToFP());
            break;

        case Kind.CONSTANT:
            loadToConstant(ec, os, reg);
            break;

        case Kind.FPUSTACK:
            // Make sure this item is on top of the FPU stack
            FPUHelper.fxch(os, stack.fpuStack, this);
            stack.fpuStack.pop(this);
            // Convert & move to new space on normal stack
            os.writeLEA(SP, SP, -4);
            popFromFPU(os, SP, 0);
            os.writePOP(reg);
            break;

        case Kind.STACK:
            //TODO: make sure this is on top os stack
            if (VirtualStack.checkOperandStack) {
                stack.operandStack.pop(this);
            }
            os.writePOP(reg);
            break;

        default:
            throw new IllegalArgumentException("Invalid item kind");
        }
        kind = Kind.REGISTER;
        this.reg = reg;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param reg
     */
    protected abstract void loadToConstant(EmitterContext ec,
            AbstractX86Stream os, Register reg);

    /**
     * Load this item to a general purpose register.
     * 
     * @param ec
     */
    final void loadToGPR(EmitterContext ec) {
        if (kind != Kind.REGISTER) {
            Register r = ec.getPool().request(JvmType.INT);
            if (r == null) {
                ec.getVStack().push(ec);
                r = ec.getPool().request(JvmType.INT);
            }
            if (Vm.VerifyAssertions)
                Vm._assert(r != null, "r != null");
            loadTo(ec, r);
        }
    }

    /**
     * Load this item into a register that is suitable for BITS8 mode.
     * 
     * @param ec
     */
    final void loadToBITS8GPR(EmitterContext ec) {
        if (!isRegister() || !reg.isSuitableForBits8()) {
            final X86RegisterPool pool = ec.getPool();
            Register r = pool.request(JvmType.INT, this, true);
            if (r == null) {
                ec.getVStack().push(ec);
                r = ec.getPool().request(JvmType.INT, this, true);
            }
            if (Vm.VerifyAssertions)
                Vm._assert(r != null, "r != null");
            loadTo(ec, r);
        }
    }

    /**
     * Load item into the given register (only for Category 1 items), if its
     * kind matches the mask.
     * 
     * @param t0
     *            the destination register
     */
    final void loadToIf(EmitterContext ec, int mask, Register t0) {
        if ((getKind() & mask) > 0)
            loadTo(ec, t0);
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

        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(reg);
            break;

        case Kind.LOCAL:
            os.writePUSH(FP, offsetToFP);
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            break;

        case Kind.FPUSTACK:
            // Make sure this item is on top of the FPU stack
            final FPUStack fpuStack = stack.fpuStack;
            FPUHelper.fxch(os, fpuStack, this);
            stack.fpuStack.pop(this);
            // Convert & move to new space on normal stack
            os.writeLEA(SP, SP, -4);
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

        default:
            throw new IllegalArgumentException("Invalid item kind");
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

        switch (getKind()) {
        case Kind.REGISTER:
            os.writePUSH(reg);
            pushToFPU(os, SP, 0);
            os.writeLEA(SP, SP, 4);
            break;

        case Kind.LOCAL:
            pushToFPU(os, FP, offsetToFP);
            break;

        case Kind.CONSTANT:
            pushConstant(ec, os);
            pushToFPU(os, SP, 0);
            os.writeLEA(SP, SP, 4);
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
            os.writeLEA(SP, SP, 4);
            break;

        default:
            throw new IllegalArgumentException("Invalid item kind");
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
        ec.getItemFactory().release(this);
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
            pool.release(reg);
            if (Vm.VerifyAssertions)
                Vm._assert(pool.isFree(reg), "reg is free");
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
            //nothing to do
            break;

        default:
            throw new IllegalArgumentException("Invalid item kind");
        }

        this.reg = null;
        this.kind = 0;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
     */
    final void spill(EmitterContext ec, Register reg) {
        if (Vm.VerifyAssertions)
            Vm._assert((getKind() == Kind.REGISTER) && (this.reg == reg),
                    "spill1");
        final X86RegisterPool pool = ec.getPool();
        Register r = pool.request(getType());
        if (r == null) {
            int cnt = ec.getVStack().push(ec);
            if (getKind() == Kind.STACK) {
                return;
            }
            r = pool.request(getType());
            System.out
                    .println("Pool state after push of " + cnt + ":\n" + pool);
            if (Vm.VerifyAssertions)
                Vm._assert(r != null, "r != null");
        }
        loadTo(ec, r);
        pool.transferOwnerTo(r, this);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
     */
    final boolean uses(Register reg) {
        return ((kind == Kind.REGISTER) && this.reg.equals(reg));
    }

    /**
     * enquire whether the item uses a volatile register
     * 
     * @param reg
     * @return true, when this item uses a volatile register.
     */
    final boolean usesVolatileRegister(X86RegisterPool pool) {
        return ((kind == Kind.REGISTER) && !pool.isCallerSaved(reg));
    }

}