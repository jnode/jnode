/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.JvmType;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 */
final class DoubleItem extends DoubleWordItem implements X86CompilerConstants  {

	private final double value;
	
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	public DoubleItem(int kind,  int offsetToFP, Register lsb, Register msb, double value) {
		super(kind, offsetToFP, lsb, msb);
		this.value = value;
	}

	/**
	 * Get the JVM type of this item
	 * @return the JVM type
	 */
	final int getType() { return JvmType.DOUBLE; }
	
    /**
     * Load my constant to the given os.
     * @param os
     * @param lsb
     * @param msb
     */
    protected final void loadToConstant(EmitterContext ec, AbstractX86Stream os, Register lsb, Register msb) {
        final long lvalue = Double.doubleToLongBits(value);
	    final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
	    final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);

		os.writeMOV_Const(lsb, lsbv);
		os.writeMOV_Const(msb, msbv);        
    }

    /**
     * Push my constant on the stack using the given os.
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        final long lvalue = Double.doubleToLongBits(value);
	    final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
	    final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
	    os.writePUSH(msbv);
	    os.writePUSH(lsbv);        
    }

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
				
			case Kind.FPUSTACK:
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

	static DoubleItem createStack() {
		return new DoubleItem(Kind.STACK, 0, null, null, 0.0);
	}
	
	static DoubleItem createLocal(int offsetToFP) {
		return new DoubleItem(Kind.LOCAL, offsetToFP, null, null, 0.0);
	}
	
	static DoubleItem createConst(double val) {
		return new DoubleItem(Kind.CONSTANT, 0, null, null, val);
	}
	
	static DoubleItem createReg(Register lsb, Register msb) {
		return new DoubleItem(Kind.REGISTER, 0, lsb, msb, 0.0);
	}
	
	static DoubleItem createFPUStack() {
		return new DoubleItem(Kind.FPUSTACK, 0, null, null, 0.0);
	}

}
