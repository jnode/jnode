/*
 * Created on 03.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jnode.vm.x86.compiler.l1a;

/**
 * @author Patrik Reali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class FloatItem extends Item {

	private float value;
	/**
	 * @param kind
	 * @param type
	 * @param local
	 */
	private FloatItem(int kind,  int local, float value) {
		super(kind, FLOAT, local);
		
		this.value = value;
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

	static FloatItem CreateStack() {
		return new FloatItem(STACK, 0, 0);
	}
	
	static FloatItem CreateLocal(int index) {
		return new FloatItem(LOCAL, index, 0);
	}
	
	static FloatItem CreateConst(float val) {
		return new FloatItem(CONSTANT, 0, val);
	}
	
	static FloatItem CreateFReg() {
		return new FloatItem(FREGISTER, 0, 0);
	}
}
