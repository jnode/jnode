/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

import org.jnode.vm.JvmType;

/**
 * @author Madhu Siddalingaiah
 *
 * An operand of an intermediate operation
 * This could be a constant, local variable, or stack entry
 */
public abstract class Operand {
	/**
	 * NOTE: these values *must* be less than 16!!
	 * @see #getAddressingMode() below
	 */
	public static final int UNKNOWN = 0;
	public static final int BYTE = JvmType.BYTE;
	public static final int SHORT = JvmType.SHORT;
	public static final int CHAR = JvmType.CHAR;
	public static final int INT = JvmType.INT;
	public static final int LONG = JvmType.LONG;
	public static final int FLOAT = JvmType.FLOAT;
	public static final int DOUBLE = JvmType.DOUBLE;
	public static final int REFERENCE = JvmType.REFERENCE;
	
	/*
	 * Addressing mode bits
	 */
	public static final int MODE_CONSTANT = 0x01;
	public static final int MODE_REGISTER = 0x02;
	public static final int MODE_STACK = 0x03;

	private int type;	// One of the above
	
	public Operand(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}

	public abstract Operand simplify();
	
	/**
	 * One of MODE_xxx constants defined above
	 * 
	 * @return
	 */
	public abstract int getAddressingMode();
}
