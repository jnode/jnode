/*
 * Created on 03.03.2004
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
final class LongItem extends Item {

	private Register lsb;
	private Register msb;
	private long value;

	/**
	 * @param kind
	 * @param type
	 * @param local
	 */
	private LongItem(int kind, int local, Register lsb, Register msb, long val) {
		super(kind, LONG, local);

		this.lsb = lsb;
		this.msb = msb;
		this.value = val;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	static LongItem CreateStack() {
		return new LongItem(STACK, 0, null, null, 0);
	}
	
	static LongItem CreateConst(long value) {
		return new LongItem(CONSTANT, 0, null, null, value);
	}
	
	static LongItem CreateReg(Register lsb, Register msb) {
		return new LongItem(REGISTER, 0, lsb, msb, 0);
	}
	
	static LongItem CreateLocal(int index) {
		return new LongItem(LOCAL, index, null, null, 0);
	}
}
