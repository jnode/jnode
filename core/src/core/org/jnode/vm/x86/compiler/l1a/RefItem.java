/*
 * Created on 02.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;

/**
 * @author Patrik Reali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class RefItem extends Item {

	private Register reg;
	private Object value;

	/**
	 * @param kind
	 * @param type
	 * @param local
	 */
	private RefItem(int kind, Register reg, Object val, int local) {
		super(kind, REFERENCE, local);
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
	

	void loadTo(Register reg) {
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
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load() {
		// TODO Auto-generated method stub
		notImplemented();
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU() {
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

	static RefItem CreateRegister(Register reg) {
		return new RefItem(REGISTER, reg, null, 0);
	}
	
	static RefItem CreateConst(Object value) {
		return new RefItem(CONSTANT, null, value, 0);
	}

	static RefItem CreateLocal(int index) {
		return new RefItem(LOCAL, null, null, index);
	}
	
	static RefItem CreateStack() {
		return new RefItem(STACK, null, null, 0);
	}
}
