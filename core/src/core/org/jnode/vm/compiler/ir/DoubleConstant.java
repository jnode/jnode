/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Levente S�ntha
 */
public class DoubleConstant extends Constant {
	private double value;
	
	public DoubleConstant(double value) {
		super(Operand.DOUBLE);
		this.value = value;
	}

	public double getValue() {
		return value;
	}
	
	public String toString() {
		return Double.toString(value);
	}
}
