/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 *
 * An operand of an intermediate operation
 * This could be a constant, local variable, or stack entry
 */
public class Operand {
	public static final int UNKNOWN = 0;
	public static final int BYTE = 1;
	public static final int SHORT = 2;
	public static final int CHAR = 3;
	public static final int INT = 4;
	public static final int LONG = 5;
	public static final int FLOAT = 6;
	public static final int DOUBLE = 7;
	public static final int REFERENCE = 8;

	private int type;	// One of the above
	
	public Operand(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	/**
	 * @param i
	 */
	public void setType(int type) {
		this.type = type;
	}
}
