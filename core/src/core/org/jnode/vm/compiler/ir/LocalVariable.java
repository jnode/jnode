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

	/**
	 * @param variable
	 */
	public LocalVariable(LocalVariable variable) {
		this(variable.getType(), variable.getIndex());
	}

	public String toString() {
		return "l" + getIndex() + '_' + getSSAValue();
	}
	
	public Object clone() {
		return new LocalVariable(this);
	}
}
