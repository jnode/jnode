/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 *
 * IntItems are items with type INT
 */

final class IntItem extends Item implements X86CompilerConstants {

	private int value;
	private Register reg;
	
	private IntItem(int kind, Register reg, int value, int local) {
		super(kind, JvmType.INT, local);
		this.value = value;
		this.reg = reg;
	}
	
	Register getRegister() {
		myAssert(kind == Kind.REGISTER);
		return reg;
	}
	
	int getValue() {
		myAssert(kind == Kind.CONSTANT);
		return value;
	}
	
	/**
	 * load item with register reg. Assumes that reg is properly allocated
	 * 
	 * @param ec current emitter context
	 * @param reg register to load the item to
	 */
	void loadTo(EmitterContext ec, Register reg) {
		myAssert(reg != null);
		AbstractX86Stream os = ec.getStream();
		X86RegisterPool pool = ec.getPool();
		myAssert(!pool.isFree(reg));
		
		switch (kind) {
			case Kind.REGISTER:
				if (this.reg != reg) {
					release(ec);
					os.writeMOV(INTSIZE, reg, this.reg);
				}
				break;
				
			case Kind.LOCAL:
				os.writeMOV(INTSIZE, reg, FP, getOffsetToFP());
				break;
				
			case Kind.CONSTANT:
				if (value != 0) {
					os.writeMOV_Const(reg, value);
				} else {
					os.writeXOR(reg, reg);
				}
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;

			case Kind.STACK:
				//TODO: make sure this is on top os stack
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
					stack.popFromOperandStack(this);
				}
				os.writePOP(reg);
		}
		kind = Kind.REGISTER;
		this.reg = reg;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadTo(org.jnode.assembler.x86.Register, org.jnode.assembler.x86.Register)
	 */
//	void loadTo(Register t0, Register t1) {
//		throw new VerifyError("Cannot load int or ref to 64 bit register");
//	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load(EmitterContext ec) {
		if (kind != Kind.REGISTER) {
			final X86RegisterPool pool = ec.getPool();
			Register r = (Register)pool.request(JvmType.INT, this);
			if (r == null) {
				final VirtualStack vstack = ec.getVStack();
				vstack.push(ec);
				r = (Register)pool.request(JvmType.INT, this);
			}
			myAssert(r != null);
			loadTo(ec, r);			
		}
	}

	/**
	 * Load item into the given register (only for Category 1 items), if its kind
	 * matches the mask.
	 * 
	 * @param t0 the destination register
	 */
	void loadToIf(EmitterContext ec, int mask, Register t0) {
		if ((getKind() & mask) > 0)
			loadTo(ec, t0);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	Item clone(EmitterContext ec) {
		Item res = null;
		switch (getKind()) {
			case Kind.REGISTER:
				final X86RegisterPool pool = ec.getPool();
				final Register r = (Register)pool.request(JvmType.INT);
				res = createRegister(r);
				pool.transferOwnerTo(r, res);
				break;
				
			case Kind.LOCAL:
				res = createLocal(getOffsetToFP());
				break;
				
			case Kind.CONSTANT:
				res = createConst(value);
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case Kind.STACK:
				AbstractX86Stream os = ec.getStream();
				os.writePUSH(Register.SP, 0);
				res = createStack();
				if (VirtualStack.checkOperandStack) {
					final VirtualStack stack = ec.getVStack();
					stack.pushOnOperandStack(res);
				}
				break;
		}
		return res;
	}


	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		
		switch (getKind()) {
			case Kind.REGISTER:
				os.writePUSH(reg);
				break;
				
			case Kind.LOCAL:
				os.writePUSH(FP, offsetToFP);
				break;
				
			case Kind.CONSTANT:
				os.writePUSH(value);
				break;
				
			case Kind.FREGISTER:
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
						stack.popFromOperandStack(this);
					}
				}
				break;

		}
		release(ec);
		kind = Kind.STACK;
		
		if (VirtualStack.checkOperandStack) {
			final VirtualStack stack = ec.getVStack();
			stack.pushOnOperandStack(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release(EmitterContext ec) {
		final X86RegisterPool pool = ec.getPool();

		switch (getKind()) {
			case Kind.REGISTER:
				pool.release(reg);
				break;
				
			case Kind.LOCAL:
				// nothing to do
				break;
				
			case Kind.CONSTANT:
				// nothing to do
				break;
				
			case Kind.FREGISTER:
				notImplemented();
				break;

			case Kind.STACK:
				//nothing to do
				break;
		}

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	void spill(EmitterContext ec, Register reg) {
		myAssert((getKind() == Kind.REGISTER) && (this.reg == reg));
		X86RegisterPool pool = ec.getPool();
		Register r = (Register)pool.request(JvmType.INT);
		myAssert(r != null);
		loadTo(ec, r);
		pool.transferOwnerTo(r, this);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return ((kind == Kind.REGISTER) && this.reg.equals(reg));
	}
	
	static IntItem createRegister(Register reg) {
		return new IntItem(Kind.REGISTER, reg, 0, 0);
	}
	
	static IntItem createConst(int value) {
		return new IntItem(Kind.CONSTANT, null, value, 0);
	}

	static IntItem createLocal(int offsetToFP) {
		return new IntItem(Kind.LOCAL, null, 0, offsetToFP);
	}
	
	static IntItem createStack() {
		return new IntItem(Kind.STACK, null, 0, 0);
	}

}
