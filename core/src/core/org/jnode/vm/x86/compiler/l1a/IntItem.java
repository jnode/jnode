/*
 * Created on 01.03.2004
 *
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;
import org.jnode.vm.compiler.ir.Constant;

/**
 * @author Patrik Reali
 *
 * IntItems are stored in a register and have type INT
 * 
 */

//TODO: maybe split in a class for each kind? It would be a nices OO design, but would create tons of classes.... could use internal classes for this
// backdraw of this is that every operation would require to generate a new item: I don't like this!

final class IntItem extends Item {

	private int value;
	private Register reg;
	
	private IntItem(int kind, Register reg, int value, int local) {
		super(kind, INT, local);
		this.value = value;
		this.reg = reg;
	}
	
	Register getRegister() {
		myAssert(getKind() == REGISTER);
		return reg;
	}
	
	int getValue() {
		myAssert(getKind() == CONSTANT);
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadTo(org.jnode.assembler.x86.Register)
	 */
	void loadTo(Register t0) {
		switch (getKind()) {
			case REGISTER:
				// nothing to do
				break;
				
			case LOCAL:
				notImplemented();
				break;
				
			case CONSTANT:
				notImplemented();
				break;
				
			case FREGISTER:
				notImplemented();
				break;
		}
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
	void loadToFPU() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * Load item into the given register (only for Category 1 items), if its kind
	 * matches the mask.
	 * Also allocate the register t0.
	 * 
	 * @param t0 the destination register
	 */
	void loadToIf(int mask, Register t0) {
		if ((getKind() & mask) > 0)
			loadTo(t0);
	}
	

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push() {
		switch (getKind()) {
			case REGISTER:
				notImplemented();
				break;
				
			case LOCAL:
				notImplemented();
				break;
				
			case CONSTANT:
				// nothing to do
				break;
				
			case FREGISTER:
				notImplemented();
				break;
		}

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release() {
		switch (getKind()) {
			case REGISTER:
				notImplemented();
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
		}

	}
	
	static IntItem CreateRegister(Register reg) {
		return new IntItem(REGISTER, reg, 0, 0);
	}
	
	static IntItem CreateConst(int value) {
		return new IntItem(CONSTANT, null, value, 0);
	}

	static IntItem CreateLocal(int index) {
		return new IntItem(LOCAL, null, 0, index);
	}
	
	static IntItem CreateStack() {
		return new IntItem(STACK, null, 0, 0);
	}
}
