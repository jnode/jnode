/*
 * Created on Nov 23, 2003
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

	public StackVariable(int type, int index, int ssaValue) {
		super(type, index, ssaValue);
	}

	/**
	 * @param variable
	 */
	public StackVariable(StackVariable variable) {
		this(variable.getType(), variable.getIndex(), variable.getSSAValue());
	}

	public String toString() {
		return "s" + getSSAValue();
	}
	
	public Object clone() {
		return new StackVariable(this);
	}
}
