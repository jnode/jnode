/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Levente Sántha
 */
public class FloatConstant extends Constant {
	private float value;
	
	public FloatConstant(float value) {
		super(Operand.FLOAT);
		this.value = value;
	}

	public float getValue() {
		return value;
	}
	
	public String toString() {
		return Float.toString(value);
	}
}
