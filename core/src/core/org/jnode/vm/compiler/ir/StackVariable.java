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
public class StackVariable extends Variable {
	/**
	 * @param type
	 * @param index
	 */
	public StackVariable(int type, int index) {
		super(type, index);
	}

	/**
	 * @param variable
	 */
	public StackVariable(StackVariable variable) {
		this(variable.getType(), variable.getIndex());
	}

	public String toString() {
		return "s" + getIndex() + '_' + getSSAValue();
	}
	
	public Object clone() {
		return new StackVariable(this);
	}
}
