/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Levente Sántha
 */
public class LongConstant extends Constant {
	private long value;
	
	public LongConstant(long value) {
		super(Operand.LONG);
		this.value = value;
	}

	public long getValue() {
		return value;
	}
	
	public String toString() {
		return Long.toString(value);
	}
}
