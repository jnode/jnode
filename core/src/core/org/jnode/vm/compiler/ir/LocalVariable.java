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
public class LocalVariable extends Variable {
	/**
	 * @param type
	 * @param index
	 */
	public LocalVariable(int type, int index) {
		super(type, index);
	}

	public LocalVariable(int type, int index, int ssaValue) {
		super(type, index, ssaValue);
	}

	/**
	 * @param variable
	 */
	public LocalVariable(LocalVariable variable) {
		this(variable.getType(), variable.getIndex(), variable.getSSAValue());
	}

	public String toString() {
		return "l" + getSSAValue();
	}
	
	public Object clone() {
		return new LocalVariable(this);
	}
}
