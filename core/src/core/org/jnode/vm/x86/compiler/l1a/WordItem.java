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
public abstract class WordItem extends Item implements X86CompilerConstants {

	private X86Register.GPR gpr;

	protected WordItem(ItemFactory factory) {
		super(factory);
	}

	protected final void initialize(int kind, X86Register reg, int local) {
		super.initialize(kind, local, ((reg instanceof X86Register.XMM) ? (X86Register.XMM)reg : null));
		this.gpr = (reg instanceof X86Register.GPR) ? (X86Register.GPR) reg
				: null;
		if (Vm.VerifyAssertions) {
			switch (kind) {
			case Kind.GPR:
				Vm._assert((this.gpr != null),
						"kind == register implies that reg != null");
				break;
			}
		}
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	protected Item clone(EmitterContext ec) {
		final WordItem res;
		final X86Assembler os = ec.getStream();

		switch (getKind()) {
		case Kind.GPR:
			res = L1AHelper.requestWordRegister(ec, getType(), false);
			final X86Register r = res.getRegister();
			os.writeMOV(INTSIZE, r, gpr);
			break;

		case Kind.LOCAL:
			res = (WordItem) factory.createLocal(getType(), getOffsetToFP());
			break;

		case Kind.CONSTANT:
			res = cloneConstant();
			break;

		case Kind.FPUSTACK:
			// TODO
			notImplemented();
			res = null;
			break;

		case Kind.STACK:
			os.writePUSH(X86Register.ESP, 0);
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
	final X86Register.GPR getRegister() {
		if (Vm.VerifyAssertions)
			Vm._assert(kind == Kind.GPR, "Must be register");
		return gpr;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
	 */
	final void load(EmitterContext ec) {
		if (kind != Kind.GPR) {
			final X86RegisterPool pool = ec.getGPRPool();
			X86Register r = pool.request(getType(), this);
			if (r == null) {
				final VirtualStack vstack = ec.getVStack();
				vstack.push(ec);
				r = pool.request(getType(), this);
			}
			if (Vm.VerifyAssertions)
				Vm._assert(r != null, "r != null");
			loadTo(ec, (X86Register.GPR) r);
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
	final void loadTo(EmitterContext ec, X86Register.GPR reg) {
		if (Vm.VerifyAssertions)
			Vm._assert(reg != null, "Reg != null");
		final X86Assembler os = ec.getStream();
		final X86RegisterPool pool = ec.getGPRPool();
		final VirtualStack stack = ec.getVStack();
		if (Vm.VerifyAssertions)
			Vm._assert(!pool.isFree(reg), "reg not free");

		switch (kind) {
		case Kind.GPR:
			if (this.gpr != reg) {
				os.writeMOV(INTSIZE, reg, this.gpr);
				cleanup(ec);
			}
			break;

		case Kind.LOCAL:
			os.writeMOV(INTSIZE, reg, X86Register.EBP, getOffsetToFP());
			break;

		case Kind.CONSTANT:
			loadToConstant(ec, os, reg);
			break;

		case Kind.FPUSTACK:
			// Make sure this item is on top of the FPU stack
			FPUHelper.fxch(os, stack.fpuStack, this);
			stack.fpuStack.pop(this);
			// Convert & move to new space on normal stack
			os.writeLEA(X86Register.ESP, X86Register.ESP, -4);
			popFromFPU(os, X86Register.ESP, 0);
			os.writePOP(reg);
			break;

		case Kind.STACK:
			// TODO: make sure this is on top os stack
			if (VirtualStack.checkOperandStack) {
				stack.operandStack.pop(this);
			}
			os.writePOP(reg);
			break;

		default:
			throw new IllegalArgumentException("Invalid item kind");
		}
		kind = Kind.GPR;
		this.gpr = reg;
	}

	/**
	 * Load my constant to the given os.
	 * 
	 * @param os
	 * @param reg
	 */
	protected abstract void loadToConstant(EmitterContext ec, X86Assembler os,
			X86Register reg);

	/**
	 * Load this item to a general purpose register.
	 * 
	 * @param ec
	 */
	final void loadToGPR(EmitterContext ec) {
		if (kind != Kind.GPR) {
			X86Register r = ec.getGPRPool().request(JvmType.INT);
			if (r == null) {
				ec.getVStack().push(ec);
				r = ec.getGPRPool().request(JvmType.INT);
			}
			if (Vm.VerifyAssertions)
				Vm._assert(r != null, "r != null");
			loadTo(ec, (X86Register.GPR) r);
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
	 * Load this item into a register that is suitable for BITS8 mode.
	 * 
	 * @param ec
	 */
	final void loadToBITS8GPR(EmitterContext ec) {
		if (!isGPR() || !gpr.isSuitableForBits8()) {
			final X86RegisterPool pool = ec.getGPRPool();
			X86Register r = pool.request(JvmType.INT, this, true);
			if (r == null) {
				ec.getVStack().push(ec);
				r = ec.getGPRPool().request(JvmType.INT, this, true);
			}
			if (Vm.VerifyAssertions)
				Vm._assert(r != null, "r != null");
			loadTo(ec, (X86Register.GPR) r);
		}
	}

	/**
	 * Load item into the given register (only for Category 1 items), if its
	 * kind matches the mask.
	 * 
	 * @param t0
	 *            the destination register
	 */
	final void loadToIf(EmitterContext ec, int mask, X86Register.GPR t0) {
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
	protected abstract void popFromFPU(X86Assembler os, X86Register reg,
			int disp);

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
	 */
	final void push(EmitterContext ec) {
		final X86Assembler os = ec.getStream();
		final VirtualStack stack = ec.getVStack();

		switch (getKind()) {
		case Kind.GPR:
			os.writePUSH(gpr);
			break;

		case Kind.LOCAL:
			os.writePUSH(X86Register.EBP, offsetToFP);
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
			os.writeLEA(X86Register.ESP, X86Register.ESP, -4);
			popFromFPU(os, X86Register.ESP, 0);
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
	protected abstract void pushConstant(EmitterContext ec, X86Assembler os);

	/**
	 * Push the value at the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected abstract void pushToFPU(X86Assembler os, X86Register reg, int disp);

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#pushToFPU(EmitterContext)
	 */
	final void pushToFPU(EmitterContext ec) {
		final X86Assembler os = ec.getStream();
		final VirtualStack stack = ec.getVStack();

		switch (getKind()) {
		case Kind.GPR:
			os.writePUSH(gpr);
			pushToFPU(os, X86Register.ESP, 0);
			os.writeLEA(X86Register.ESP, X86Register.ESP, 4);
			break;

		case Kind.LOCAL:
			pushToFPU(os, X86Register.EBP, offsetToFP);
			break;

		case Kind.CONSTANT:
			pushConstant(ec, os);
			pushToFPU(os, X86Register.ESP, 0);
			os.writeLEA(X86Register.ESP, X86Register.ESP, 4);
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
			pushToFPU(os, X86Register.ESP, 0);
			os.writeLEA(X86Register.ESP, X86Register.ESP, 4);
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
		// assertCondition(!ec.getVStack().contains(this), "Cannot release while
		// on vstack");
		final X86RegisterPool pool = ec.getGPRPool();

		switch (getKind()) {
		case Kind.GPR:
			pool.release(gpr);
			if (Vm.VerifyAssertions)
				Vm._assert(pool.isFree(gpr), "reg is free");
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

		default:
			throw new IllegalArgumentException("Invalid item kind");
		}

		this.gpr = null;
		this.kind = 0;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	final void spill(EmitterContext ec, X86Register reg) {
		if (Vm.VerifyAssertions) {
			Vm._assert(isGPR() && (this.gpr == reg), "spill1");
		}
		final X86RegisterPool pool = ec.getGPRPool();
		X86Register r = pool.request(getType());
		if (r == null) {
			int cnt = ec.getVStack().push(ec);
			if (getKind() == Kind.STACK) {
				return;
			}
			r = pool.request(getType());
			// System.out.println("Pool state after push of " + cnt + ":\n" +
			// pool);
			if (Vm.VerifyAssertions)
				Vm._assert(r != null, "r != null");
		}
		loadTo(ec, (X86Register.GPR) r);
		pool.transferOwnerTo(r, this);
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	final boolean uses(X86Register reg) {
		return ((kind == Kind.GPR) && this.gpr.equals(reg));
	}

	/**
	 * enquire whether the item uses a volatile register
	 * 
	 * @param reg
	 * @return true, when this item uses a volatile register.
	 */
	final boolean usesVolatileRegister(X86RegisterPool pool) {
		return ((kind == Kind.GPR) && !pool.isCallerSaved(gpr));
	}

}
