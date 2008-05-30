/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public abstract class Constant<T> extends Operand<T> {
    public Constant(int type) {
        super(type);
    }

    public static <T> Constant<T> getInstance(int value) {
        return new IntConstant<T>(value);
    }

    public static <T> Constant<T> getInstance(long value) {
        return new LongConstant<T>(value);
    }

    public static <T> Constant<T> getInstance(float value) {
        return new FloatConstant<T>(value);
    }

    public static <T> Constant<T> getInstance(double value) {
        return new DoubleConstant<T>(value);
    }

    public static final <T> Constant<T> getInstance(Object value) {
        return new ReferenceConstant<T>(value);
    }

    ///////////////// UNARY OPERATIONS ////////////////////
    public Constant<T> i2l() {
        int i = ((IntConstant<T>) this).getValue();
        return new LongConstant<T>(i);
    }

    public Constant<T> i2f() {
        int i = ((IntConstant<T>) this).getValue();
        return new FloatConstant<T>(i);
    }

    public Constant<T> i2d() {
        int i = ((IntConstant<T>) this).getValue();
        return new DoubleConstant<T>(i);
    }

    public Constant<T> l2i() {
        long l = ((LongConstant<T>) this).getValue();
        return new IntConstant<T>((int) l);
    }

    public Constant<T> l2f() {
        long l = ((LongConstant<T>) this).getValue();
        return new FloatConstant<T>(l);
    }

    public Constant<T> l2d() {
        long l = ((LongConstant<T>) this).getValue();
        return new DoubleConstant<T>(l);
    }

    public Constant<T> f2i() {
        float f = ((FloatConstant<T>) this).getValue();
        return new IntConstant<T>((int) f);
    }

    public Constant<T> f2l() {
        float f = ((FloatConstant<T>) this).getValue();
        return new LongConstant<T>((long) f);
    }

    public Constant<T> f2d() {
        float f = ((FloatConstant<T>) this).getValue();
        return new DoubleConstant<T>(f);
    }

    public Constant<T> d2i() {
        double d = ((DoubleConstant<T>) this).getValue();
        return new IntConstant<T>((int) d);
    }

    public Constant<T> d2l() {
        double d = ((DoubleConstant<T>) this).getValue();
        return new LongConstant<T>((long) d);
    }

    public Constant<T> d2f() {
        double d = ((DoubleConstant<T>) this).getValue();
        return new FloatConstant<T>((float) d);
    }

    public Constant<T> i2b() {
        int i = ((IntConstant<T>) this).getValue();
        return new IntConstant<T>((byte) i);
    }

    public Constant<T> i2c() {
        int i = ((IntConstant<T>) this).getValue();
        return new IntConstant<T>((char) i);
    }

    public Constant<T> i2s() {
        int i = ((IntConstant<T>) this).getValue();
        return new IntConstant<T>((short) i);
    }

    public Constant<T> iNeg() {
        int i = ((IntConstant<T>) this).getValue();
        return new IntConstant<T>(-i);
    }

    public Constant<T> fNeg() {
        float f = ((FloatConstant<T>) this).getValue();
        return new FloatConstant<T>(-f);
    }

    public Constant<T> lNeg() {
        long l = ((LongConstant<T>) this).getValue();
        return new LongConstant<T>(-l);
    }

    public Constant<T> dNeg() {
        double d = ((DoubleConstant<T>) this).getValue();
        return new DoubleConstant<T>(-d);
    }


    ///////////////// BINARY OPERATIONS ////////////////////
    /**
     * @param c2
     */
    public Constant<T> iAdd(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 + i2);
    }

    /**
     * @param c2
     */
    public Constant<T> iSub(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 - i2);
    }

    /**
     * @param c2
     */
    public Constant<T> iMul(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 * i2);
    }

    /**
     * @param c2
     */
    public Constant<T> iDiv(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 / i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iRem(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 % i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iAnd(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 & i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iOr(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 | i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iXor(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 ^ i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iShl(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 << i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iShr(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 >> i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> iUshr(Constant<T> c2) {
        int i1 = ((IntConstant<T>) this).getValue();
        int i2 = ((IntConstant<T>) c2).getValue();
        return new IntConstant<T>(i1 >>> i2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lAdd(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 + l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lSub(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 - l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lMul(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 * l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lDiv(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 / l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lRem(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 % l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lAnd(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 & l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lOr(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 | l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lXor(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 ^ l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lShl(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 << l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lShr(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 >> l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> lUshr(Constant<T> c2) {
        long l1 = ((LongConstant<T>) this).getValue();
        long l2 = ((LongConstant<T>) c2).getValue();
        return new LongConstant<T>(l1 >>> l2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> fAdd(Constant<T> c2) {
        float f1 = ((FloatConstant<T>) this).getValue();
        float f2 = ((FloatConstant<T>) c2).getValue();
        return new FloatConstant<T>(f1 + f2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> fSub(Constant<T> c2) {
        float f1 = ((FloatConstant<T>) this).getValue();
        float f2 = ((FloatConstant<T>) c2).getValue();
        return new FloatConstant<T>(f1 - f2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> fMul(Constant<T> c2) {
        float f1 = ((FloatConstant<T>) this).getValue();
        float f2 = ((FloatConstant<T>) c2).getValue();
        return new FloatConstant<T>(f1 * f2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> fDiv(Constant<T> c2) {
        float f1 = ((FloatConstant<T>) this).getValue();
        float f2 = ((FloatConstant<T>) c2).getValue();
        return new FloatConstant<T>(f1 / f2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> fRem(Constant<T> c2) {
        float f1 = ((FloatConstant<T>) this).getValue();
        float f2 = ((FloatConstant<T>) c2).getValue();
        return new FloatConstant<T>(f1 / f2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> dAdd(Constant<T> c2) {
        double d1 = ((DoubleConstant<T>) this).getValue();
        double d2 = ((DoubleConstant<T>) c2).getValue();
        return new DoubleConstant<T>(d1 + d2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> dSub(Constant<T> c2) {
        double d1 = ((DoubleConstant<T>) this).getValue();
        double d2 = ((DoubleConstant<T>) c2).getValue();
        return new DoubleConstant<T>(d1 - d2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> dMul(Constant<T> c2) {
        double d1 = ((DoubleConstant<T>) this).getValue();
        double d2 = ((DoubleConstant<T>) c2).getValue();
        return new DoubleConstant<T>(d1 * d2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> dDiv(Constant<T> c2) {
        double d1 = ((DoubleConstant<T>) this).getValue();
        double d2 = ((DoubleConstant<T>) c2).getValue();
        return new DoubleConstant<T>(d1 / d2);
    }

    /**
     * @param c2
     * @return
     */
    public Constant<T> dRem(Constant<T> c2) {
        double d1 = ((DoubleConstant<T>) this).getValue();
        double d2 = ((DoubleConstant<T>) c2).getValue();
        return new DoubleConstant<T>(d1 % d2);
    }

    /**
     * @see org.jnode.vm.compiler.ir.Operand#simplify()
     */
    public Operand<T> simplify() {
        return this;
    }

    /**
     * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
     */
    public AddressingMode getAddressingMode() {
        return AddressingMode.CONSTANT;
    }
}
