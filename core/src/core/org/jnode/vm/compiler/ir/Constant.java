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
public abstract class Constant extends Operand {
	public Constant(int type) {
		super(type);
	}
	
	public static Constant getInstance(byte value) {
		throw new IllegalArgumentException("constant not yet defined");
	}
	
	public static Constant getInstance(short value) {
		throw new IllegalArgumentException("constant not yet defined");
	}
	
	public static Constant getInstance(char value) {
		throw new IllegalArgumentException("constant not yet defined");
	}

	public static Constant getInstance(int value) {
		return new IntConstant(value);
	}
	
	public static Constant getInstance(long value) {
		throw new IllegalArgumentException("constant not yet defined");
	}
	
	public static Constant getInstance(float value) {
		throw new IllegalArgumentException("constant not yet defined");
	}
	
	public static Constant getInstance(double value) {
		throw new IllegalArgumentException("constant not yet defined");
	}
	
	public static Constant getInstance(Object value) {
		return new ReferenceConstant(value);
	}
	
	/**
	 * @param c2
	 */
	public Constant iAdd(Constant c2) {
		int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 + i2);
	}

	/**
	 * @param c2
	 */
	public Constant iSub(Constant c2) {
		int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 - i2);
	}

	/**
	 * @param c2
	 */
	public Constant iMul(Constant c2) {
		int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 * i2);
	}

	/**
	 * @param c2
	 */
	public Constant iDiv(Constant c2) {
		int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 / i2);
	}
}
