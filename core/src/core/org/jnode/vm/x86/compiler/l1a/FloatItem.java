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
		super(kind, FLOAT, offsetToFP);
		
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
	 * @see org.jnode.vm.x86.compiler.l1a.Item#push()
	 */
	void push(EmitterContext ec) {
		final AbstractX86Stream os = ec.getStream();
		
		switch (getKind()) {
			case REGISTER:
				//TODO
				notImplemented();
				break;
				
			case LOCAL:
				os.writePUSH(FP, offsetToFP);
				break;
				
			case CONSTANT:
				os.writePUSH(Float.floatToRawIntBits(value));
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
		}
		release(ec);
		kind = STACK;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#release()
	 */
	void release(EmitterContext ec) {
		switch (getKind()) {
			case REGISTER:
				//TODO
				notImplemented();
				break;
				
			case LOCAL:
				// nothing to do
				break;
				
			case CONSTANT:
				// nothing to do
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.x86.compiler.l1a.Item#uses(org.jnode.assembler.x86.Register)
	 */
	boolean uses(Register reg) {
		return false;
	}

	static FloatItem createStack() {
		return new FloatItem(STACK, 0, 0);
	}
	
	static FloatItem createLocal(int offsetToFP) {
		return new FloatItem(LOCAL, offsetToFP, 0);
	}
	
	static FloatItem createConst(float val) {
		return new FloatItem(CONSTANT, 0, val);
	}
	
	static FloatItem createFReg() {
		return new FloatItem(FREGISTER, 0, 0);
	}
}
