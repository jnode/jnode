/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class IntConstant extends Constant {
	private int value;
	
	public IntConstant(int value) {
		super(Operand.INT);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
}
