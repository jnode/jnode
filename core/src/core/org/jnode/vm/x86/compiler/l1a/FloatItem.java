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
final class FloatItem extends Item implements X86CompilerConstants  {

	private float value;
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	private FloatItem(int kind,  int offsetToFP, float value) {
		super(kind, JvmType.FLOAT, offsetToFP);
		
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
//	void loadToFPU(EmitterContext ec) {
//		// TODO Auto-generated method stub
//		notImplemented();
//	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		
		switch (getKind()) {
			case Kind.REGISTER:
				//TODO
				notImplemented();
				break;
				
			case Kind.LOCAL:
				os.writePUSH(FP, offsetToFP);
				break;
				
			case Kind.CONSTANT:
				os.writePUSH(Float.floatToRawIntBits(value));
				break;
				
			case Kind.FREGISTER:
				//TODO
				notImplemented();
				break;

			case Kind.STACK:
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return false;
	}

	static FloatItem createStack() {
		return new FloatItem(Kind.STACK, 0, 0);
	}
	
	static FloatItem createLocal(int offsetToFP) {
		return new FloatItem(Kind.LOCAL, offsetToFP, 0);
	}
	
	static FloatItem createConst(float val) {
		return new FloatItem(Kind.CONSTANT, 0, val);
	}
	
	static FloatItem createFReg() {
		return new FloatItem(Kind.FREGISTER, 0, 0);
	}
}
