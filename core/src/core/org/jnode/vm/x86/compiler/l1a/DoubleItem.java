/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

/**
 * @author Patrik Reali
 */
final class DoubleItem extends Item {

	private double value;
	
	/**
	 * @param kind
	 * @param local
	 * @param value
	 */
	public DoubleItem(int kind,  int local, double value) {
		super(kind, DOUBLE, local);
		
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
	void loadToFPU() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push() {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release() {
		// TODO Auto-generated method stub
		notImplemented();

	}
	
	static DoubleItem CreateStack() {
		return new DoubleItem(STACK, 0, 0);
	}
	
	static DoubleItem CreateLocal(int index) {
		return new DoubleItem(LOCAL, index, 0);
	}
	
	static DoubleItem CreateConst(double val) {
		return new DoubleItem(CONSTANT, 0, val);
	}
	
	static DoubleItem CreateFReg() {
		return new DoubleItem(FREGISTER, 0, 0);
	}

}
