/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.*;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class LongItem extends DoubleWordItem  implements X86CompilerConstants {

	private final long value;

	/**
	 * @param kind
	 * @param offsetToFP
	 * @param lsb
	 * @param msb
	 * @param val
	 */
	private LongItem(int kind, int offsetToFP, Register lsb, Register msb, long val) {
		super(kind, offsetToFP, lsb, msb);
		this.value = val;
	}


	/**
	 * Get the JVM type of this item
	 * @return the JVM type
	 */
	final int getType() { return JvmType.LONG; }
	
    /**
     * Gets the constant value of this item.
     * @return
     */
    final long getValue() {
        myAssert(kind == Kind.CONSTANT);
        return value;
    }

    /**
     * Gets the LSB part of the constant value of this item.
     * @return
     */
    final int getLsbValue() {
        myAssert(kind == Kind.CONSTANT);
        return (int)(value & 0xFFFFFFFFL);
    }

    /**
     * Gets the MSB part of the constant value of this item.
     * @return
     */
    final int getMsbValue() {
        myAssert(kind == Kind.CONSTANT);
        return (int)((value >>> 32) & 0xFFFFFFFFL);
    }
    
    /**
     * Load my constant to the given os.
     * @param os
     * @param lsb
     * @param msb
     */
    protected final void loadToConstant(EmitterContext ec, AbstractX86Stream os, Register lsb, Register msb) {
	    final int lsbv = (int) (value & 0xFFFFFFFFL);
	    final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

		os.writeMOV_Const(lsb, lsbv);
		os.writeMOV_Const(msb, msbv);        
    }

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
	 */
	final Item clone(EmitterContext ec) {
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

    /**
     * Push my constant on the stack using the given os.
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, AbstractX86Stream os) {
	    os.writePUSH(getMsbValue());
	    os.writePUSH(getLsbValue());        
    }

	
	static LongItem createStack() {
		return new LongItem(Kind.STACK, 0, null, null, 0);
	}
	
	static LongItem createConst(long value) {
		return new LongItem(Kind.CONSTANT, 0, null, null, value);
	}
	
	static LongItem createReg(Register lsb, Register msb) {
		return new LongItem(Kind.REGISTER, 0, lsb, msb, 0);
	}
	
	static LongItem createLocal(int offsetToFP) {
		return new LongItem(Kind.LOCAL, offsetToFP, null, null, 0);
	}
}
