/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 */
 final class RefItem extends Item implements X86CompilerConstants {

	private Register reg;
	private Object value;

	/**
	 * @param kind
	 * @param reg
	 * @param val
	 * @param offsetToFP
	 */
	private RefItem(int kind, Register reg, Object val, int offsetToFP) {
		super(kind, REFERENCE, offsetToFP);
		this.reg = reg;
		this.value = val;
	}

	Register getRegister() {
		myAssert(getKind() == REGISTER);
		return reg;
	}
	
	Object getValue() {
		myAssert(getKind() == CONSTANT);
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

		switch (getKind()) {
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
				notImplemented();
				break;
				
			case FREGISTER:
				notImplemented();
				break;
				
			case STACK:
				os.writePOP(reg);
				break;

		}
		kind = REGISTER;
		this.reg = reg;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

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
				//TODO
				notImplemented();
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
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return ((kind == REGISTER) && this.reg.equals(reg));
	}

	static RefItem createRegister(Register reg) {
		return new RefItem(REGISTER, reg, null, 0);
	}
	
	static RefItem createConst(Object value) {
		return new RefItem(CONSTANT, null, value, 0);
	}

	static RefItem createLocal(int offsetToFP) {
		return new RefItem(LOCAL, null, null, offsetToFP);
	}
	
	static RefItem createStack() {
		return new RefItem(STACK, null, null, 0);
	}


}
