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
final class DoubleItem extends Item implements X86CompilerConstants  {

	private double value;
	
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	public DoubleItem(int kind,  int offsetToFP, double value) {
		super(kind, offsetToFP);
		
		this.value = value;
	}

	/**
	 * Get the JVM type of this item
	 * @return the JVM type
	 */
	int getType() { return JvmType.DOUBLE; }
	
    /**
     * Return the current item's computational type category (JVM Spec, p. 83).
     * In practice, this is the number of double words needed by the item (1 or
     * 2)
     * 
     * @return computational type category
     */
    final int getCategory() {
        return 2;
    }

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load(EmitterContext)
	 */
	void load(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * @//see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
//	void loadToFPU(EmitterContext ec) {
//		// TODO Auto-generated method stub
//		notImplemented();
//	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	Item clone(EmitterContext ec) {
		Item res = null;
		switch (getKind()) {
			case Kind.REGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.LOCAL:
				//TODO
				notImplemented();
				break;
				
			case Kind.CONSTANT:
				//TODO
				notImplemented();
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
			
			case Kind.STACK:
				//TODO
				notImplemented();
				break;
		}
		return res;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push(EmitterContext)
	 */
	void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		
		switch (getKind()) {
			case Kind.REGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.LOCAL:
				os.writePUSH(FP, offsetToFP+4);
				os.writePUSH(FP, offsetToFP);
				break;
				
			case Kind.CONSTANT:
				final long v = Double.doubleToLongBits(value);
			    final int lsb = (int) (v & 0xFFFFFFFFL);
			    final int msb = (int) ((v >>> 32) & 0xFFFFFFFFL);

			    os.writePUSH(lsb);
			    os.writePUSH(msb);
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;

			case Kind.STACK:
				// nothing to do
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

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release(EmitterContext)
	 */
	void release(EmitterContext ec) {
		switch (getKind()) {
			case Kind.REGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.LOCAL:
				// nothing to do
				break;
				
			case Kind.CONSTANT:
				// nothing to do
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;
		}
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#spill(EmitterContext, Register)
	 */
	void spill(EmitterContext ec, Register reg) {
		notImplemented();
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return false;
	}

	static DoubleItem createStack() {
		return new DoubleItem(Kind.STACK, 0, 0);
	}
	
	static DoubleItem createLocal(int offsetToFP) {
		return new DoubleItem(Kind.LOCAL, offsetToFP, 0);
	}
	
	static DoubleItem createConst(double val) {
		return new DoubleItem(Kind.CONSTANT, 0, val);
	}
	
	static DoubleItem createFReg() {
		return new DoubleItem(Kind.FREGISTER, 0, 0);
	}

}
