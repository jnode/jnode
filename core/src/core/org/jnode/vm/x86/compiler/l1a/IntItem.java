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
		super(kind, INT, local);
		this.value = value;
		this.reg = reg;
	}
	
	Register getRegister() {
		myAssert(kind == REGISTER);
		return reg;
	}
	
	int getValue() {
		myAssert(kind == CONSTANT);
		return value;
	}
	
	/**
	 * load item with register reg. Assumes that reg is properly allocated
	 * 
	 * @param ec current emitter context
	 * @param reg register to load the item to
	 */
	void loadTo(EmitterContext ec, Register reg) {
		AbstractX86Stream os = ec.getStream();
		X86RegisterPool pool = ec.getPool();
		myAssert(!pool.isFree(reg));
		
		switch (kind) {
			case REGISTER:
				if (this.reg != reg) {
					release(ec);
					os.writeMOV(INTSIZE, reg, this.reg);
				}
				break;
				
			case LOCAL:
				os.writeMOV(INTSIZE, reg, FP, getOffsetToFP());
				break;
				
			case CONSTANT:
				if (value != 0) {
					os.writeMOV_Const(reg, value);
				} else {
					os.writeXOR(reg, reg);
				}
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
			case STACK:
				//TODO: make sure this is on top os stack
				os.writePOP(reg);
		}
		kind = REGISTER;
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
		if (kind != REGISTER) {
			final X86RegisterPool pool = ec.getPool();
			final Register r = (Register)pool.request(INT, this);
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
			case REGISTER:
				final X86RegisterPool pool = ec.getPool();
				final Register r = (Register)pool.request(INT);
				res = createRegister(r);
				pool.transferOwnerTo(r, res);
				break;
				
			case LOCAL:
				res = createLocal(getOffsetToFP());
				break;
				
			case CONSTANT:
				res = createConst(value);
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case STACK:
				AbstractX86Stream os = ec.getStream();
				os.writePUSH(Register.SP, 0);
				res = createStack();
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
			case REGISTER:
				os.writePUSH(reg);
				break;
				
			case LOCAL:
				os.writePUSH(FP, offsetToFP);
				break;
				
			case CONSTANT:
				os.writePUSH(value);
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case STACK:
				//nothing to do
				break;
		}
		release(ec);
		kind = STACK;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release(EmitterContext ec) {
		final X86RegisterPool pool = ec.getPool();

		switch (getKind()) {
			case REGISTER:
				pool.release(reg);
				break;
				
			case LOCAL:
				// nothing to do
				break;
				
			case CONSTANT:
				// nothing to do
				break;
				
			case FREGISTER:
				notImplemented();
				break;

			case STACK:
				//nothing to do
				break;
		}

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	void spill(EmitterContext ec, Register reg) {
		myAssert((getKind() == REGISTER) && (this.reg == reg));
		X86RegisterPool pool = ec.getPool();
		Register r = (Register)pool.request(INT);
		myAssert(r != null);
		loadTo(ec, r);
		pool.transferOwnerTo(r, this);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return ((kind == REGISTER) && this.reg.equals(reg));
	}
	
	static IntItem createRegister(Register reg) {
		return new IntItem(REGISTER, reg, 0, 0);
	}
	
	static IntItem createConst(int value) {
		return new IntItem(CONSTANT, null, value, 0);
	}

	static IntItem createLocal(int offsetToFP) {
		return new IntItem(LOCAL, null, 0, offsetToFP);
	}
	
	static IntItem createStack() {
		return new IntItem(STACK, null, 0, 0);
	}

}
