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
public class ReferenceConstant extends Constant {
	private Object value;

	/**
	 * @param value
	 */
	public ReferenceConstant(Object value) {
		super(Operand.REFERENCE);
		this.value = value;
	}
	
	public String toString() {
		if (value == null) {
			return "null";
		}
		return value.toString();
	}
}
