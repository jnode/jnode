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
public class MethodArgument extends Variable {
	/**
	 * @param type
	 * @param index
	 */
	public MethodArgument(int type, int index) {
		super(type, index);
	}

	public MethodArgument(int type, int index, int ssaValue) {
		super(type, index, ssaValue);
	}

	/**
	 * @param argument
	 */
	public MethodArgument(MethodArgument argument) {
		this(argument.getType(), argument.getIndex(), argument.getSSAValue());
	}

	public String toString() {
		return "a" + getSSAValue();
	}
	
	public Object clone() {
		return new MethodArgument(this);
	}
}
