/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public abstract class Constant extends Operand {
	public Constant(int type) {
		super(type);
	}
	
	public static Constant getInstance(int value) {
		return new IntConstant(value);
	}
	
	public static Constant getInstance(long value) {
		return new LongConstant(value);
	}
	
	public static Constant getInstance(float value) {
		return new FloatConstant(value);
	}
	
	public static Constant getInstance(double value) {
		return new DoubleConstant(value);
	}
	
	public static Constant getInstance(Object value) {
		return new ReferenceConstant(value);
	}

    ///////////////// UNARY OPERATIONS ////////////////////
    public Constant i2l() {
        int i = ((IntConstant) this).getValue();
        return new LongConstant(i);
    }

    public Constant i2f() {
        int i = ((IntConstant) this).getValue();
        return new FloatConstant(i);
    }

    public Constant i2d() {
        int i = ((IntConstant) this).getValue();
        return new DoubleConstant(i);
    }

    public Constant l2i() {
        long l = ((LongConstant) this).getValue();
        return new IntConstant((int) l);
    }

    public Constant l2f() {
        long l = ((LongConstant) this).getValue();
        return new FloatConstant(l);
    }

    public Constant l2d() {
        long l = ((LongConstant) this).getValue();
        return new DoubleConstant(l);
    }

    public Constant f2i() {
        float f = ((FloatConstant) this).getValue();
        return new IntConstant((int) f);
    }

    public Constant f2l() {
        float f = ((FloatConstant) this).getValue();
        return new LongConstant((long) f);
    }

    public Constant f2d() {
        float f = ((FloatConstant) this).getValue();
        return new DoubleConstant(f);
    }

    public Constant d2i() {
        double d = ((DoubleConstant) this).getValue();
        return new IntConstant((int) d);
    }

    public Constant d2l() {
        double d = ((DoubleConstant) this).getValue();
        return new LongConstant((long) d);
    }

    public Constant d2f() {
        double d = ((DoubleConstant) this).getValue();
        return new FloatConstant((float) d);
    }

    public Constant i2b() {
        int i = ((IntConstant) this).getValue();
        return new IntConstant((byte) i);
    }

    public Constant i2c() {
        int i = ((IntConstant) this).getValue();
        return new IntConstant((char) i);
    }

    public Constant i2s() {
        int i = ((IntConstant) this).getValue();
        return new IntConstant((short) i);
    }

    public Constant iNeg() {
        int i = ((IntConstant) this).getValue();
        return new IntConstant(-i);
    }

    public Constant fNeg() {
        float f = ((FloatConstant) this).getValue();
        return new FloatConstant(-f);
    }

    public Constant lNeg() {
        long l = ((LongConstant) this).getValue();
        return new LongConstant(-l);
    }

    public Constant dNeg() {
        double d = ((DoubleConstant) this).getValue();
        return new DoubleConstant(-d);
    }


    ///////////////// BINARY OPERATIONS ////////////////////
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

    /**
     *
     * @param c2
     * @return
     */
    public Constant iRem(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 % i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iAnd(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 & i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iOr(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 | i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iXor(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 ^ i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iShl(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 << i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iShr(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 >> i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant iUshr(Constant c2) {
        int i1 = ((IntConstant) this).getValue();
		int i2 = ((IntConstant) c2).getValue();
		return new IntConstant(i1 >>> i2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lAdd(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 + l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lSub(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 - l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lMul(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 * l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lDiv(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 / l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lRem(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 % l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lAnd(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 & l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lOr(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 | l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lXor(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 ^ l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lShl(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 << l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lShr(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 >> l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant lUshr(Constant c2) {
        long l1 = ((LongConstant) this).getValue();
		long l2 = ((LongConstant) c2).getValue();
		return new LongConstant(l1 >>> l2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant fAdd(Constant c2) {
        float f1 = ((FloatConstant) this).getValue();
		float f2 = ((FloatConstant) c2).getValue();
		return new FloatConstant(f1 + f2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant fSub(Constant c2) {
        float f1 = ((FloatConstant) this).getValue();
		float f2 = ((FloatConstant) c2).getValue();
		return new FloatConstant(f1 - f2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant fMul(Constant c2) {
        float f1 = ((FloatConstant) this).getValue();
		float f2 = ((FloatConstant) c2).getValue();
		return new FloatConstant(f1 * f2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant fDiv(Constant c2) {
        float f1 = ((FloatConstant) this).getValue();
		float f2 = ((FloatConstant) c2).getValue();
		return new FloatConstant(f1 / f2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant fRem(Constant c2) {
        float f1 = ((FloatConstant) this).getValue();
		float f2 = ((FloatConstant) c2).getValue();
		return new FloatConstant(f1 / f2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant dAdd(Constant c2) {
        double d1 = ((DoubleConstant) this).getValue();
		double d2 = ((DoubleConstant) c2).getValue();
		return new DoubleConstant(d1 + d2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant dSub(Constant c2) {
        double d1 = ((DoubleConstant) this).getValue();
		double d2 = ((DoubleConstant) c2).getValue();
		return new DoubleConstant(d1 - d2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant dMul(Constant c2) {
        double d1 = ((DoubleConstant) this).getValue();
		double d2 = ((DoubleConstant) c2).getValue();
		return new DoubleConstant(d1 * d2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant dDiv(Constant c2) {
        double d1 = ((DoubleConstant) this).getValue();
		double d2 = ((DoubleConstant) c2).getValue();
		return new DoubleConstant(d1 / d2);
    }

    /**
     *
     * @param c2
     * @return
     */
    public Constant dRem(Constant c2) {
        double d1 = ((DoubleConstant) this).getValue();
		double d2 = ((DoubleConstant) c2).getValue();
		return new DoubleConstant(d1 % d2);
    }

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#simplify()
	 */
	public Operand simplify() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
	 */
	public int getAddressingMode() {
		return Operand.MODE_CONSTANT;
	}
}
