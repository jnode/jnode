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

	/**
	 * @param argument
	 */
	public MethodArgument(MethodArgument argument) {
		this(argument.getType(), argument.getIndex());
	}

	public String toString() {
		return "a" + getIndex() + '_' + getSSAValue();
	}
	
	public Object clone() {
		return new MethodArgument(this);
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#simplify()
	 */
	public Operand simplify() {
		// Can't do much with this...
		return this;
	}
}
