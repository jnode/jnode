/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
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
		super(kind, DOUBLE, offsetToFP);
		
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#load()
	 */
	void load(EmitterContext ec) {
		// TODO Auto-generated method stub
		notImplemented();

	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#loadToFPU()
	 */
//	void loadToFPU(EmitterContext ec) {
//		// TODO Auto-generated method stub
//		notImplemented();
//	}

	/**
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
				os.writePUSH(FP, offsetToFP+4);
				os.writePUSH(FP, offsetToFP);
				break;
				
			case CONSTANT:
				final long v = Double.doubleToLongBits(value);
			    final int lsb = (int) (v & 0xFFFFFFFFL);
			    final int msb = (int) ((v >>> 32) & 0xFFFFFFFFL);

			    os.writePUSH(lsb);
			    os.writePUSH(msb);
				break;
				
			case FREGISTER:
				//TODO
				notImplemented();
				break;
		}
		release(ec);
		kind = STACK;
	}

	/**
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
	
	static DoubleItem createStack() {
		return new DoubleItem(STACK, 0, 0);
	}
	
	static DoubleItem createLocal(int offsetToFP) {
		return new DoubleItem(LOCAL, offsetToFP, 0);
	}
	
	static DoubleItem createConst(double val) {
		return new DoubleItem(CONSTANT, 0, val);
	}
	
	static DoubleItem createFReg() {
		return new DoubleItem(FREGISTER, 0, 0);
	}

}
