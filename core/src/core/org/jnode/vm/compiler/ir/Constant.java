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
public class Constant extends Operand {
	private Object value;

	public Constant(int type, Object value) {
		super(type);
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	public boolean equals(Object other) {
		if (other == value) {
			return true;
		}
		if (other == null || value == null) {
			return false;
		}
		return other.equals(value);
	}

	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return value.toString();
	}
}
