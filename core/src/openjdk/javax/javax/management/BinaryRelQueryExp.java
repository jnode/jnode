/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package javax.management;


/**
 * This class is used by the query-building mechanism to represent binary
 * operations.
 * @serial include
 *
 * @since 1.5
 */
class BinaryRelQueryExp extends QueryEval implements QueryExp { 

    /* Serial version */
    private static final long serialVersionUID = -5690656271650491000L;

    /** 
     * @serial The operator 
     */
    private int relOp;

    /** 
     * @serial The first value 
     */
    private ValueExp exp1;

    /** 
     * @serial The second value 
     */
    private ValueExp exp2;


    /**
     * Basic Constructor.
     */
    public BinaryRelQueryExp() { 
    } 

    /**
     * Creates a new BinaryRelQueryExp with operator op applied on v1 and
     * v2 values.
     */
    public BinaryRelQueryExp(int op, ValueExp v1, ValueExp v2) { 
	relOp = op;
	exp1  = v1;
	exp2  = v2;
    } 


    /**
     * Returns the operator of the query.
     */   
    public int getOperator()  { 
	return relOp;
    } 

    /**
     * Returns the left value of the query.
     */
    public ValueExp getLeftValue()  { 
	return exp1;
    } 
    
    /**
     * Returns the right value of the query.
     */   
    public ValueExp getRightValue()  { 
	return exp2;
    } 
    
    /**
     * Applies the BinaryRelQueryExp on an MBean.
     *
     * @param name The name of the MBean on which the BinaryRelQueryExp will be applied.
     *
     * @return  True if the query was successfully applied to the MBean, false otherwise.
     *
     * @exception BadStringOperationException
     * @exception BadBinaryOpValueExpException
     * @exception BadAttributeValueExpException 
     * @exception InvalidApplicationException
     */
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
	BadAttributeValueExpException, InvalidApplicationException  { 
	Object val1 = exp1.apply(name);
	Object val2 = exp2.apply(name);
	String sval1;
	String sval2;
	double dval1;
	double dval2;
	long   lval1;
	long   lval2;
	boolean bval1;
	boolean bval2;
	boolean numeric = val1 instanceof NumericValueExp;
	boolean bool = val1 instanceof BooleanValueExp;
	if (numeric) {
	    if (((NumericValueExp)val1).isLong()) {
		lval1 = ((NumericValueExp)val1).longValue();
		lval2 = ((NumericValueExp)val2).longValue();

		switch (relOp) {
		case Query.GT:
		    return lval1 > lval2;
		case Query.LT:
		    return lval1 < lval2;
		case Query.GE:
		    return lval1 >= lval2;
		case Query.LE:
		    return lval1 <= lval2;
		case Query.EQ:
		    return lval1 == lval2;
		}
	    } else {
		dval1 = ((NumericValueExp)val1).doubleValue();
		dval2 = ((NumericValueExp)val2).doubleValue();

		switch (relOp) {
		case Query.GT:
		    return dval1 > dval2;
		case Query.LT:
		    return dval1 < dval2;
		case Query.GE:
		    return dval1 >= dval2;
		case Query.LE:
		    return dval1 <= dval2;
		case Query.EQ:
		    return dval1 == dval2;
		}
	    }
	    
	} else if (bool) {

	    bval1 = ((BooleanValueExp)val1).getValue().booleanValue();
	    bval2 = ((BooleanValueExp)val2).getValue().booleanValue();

	    switch (relOp) {
	    case Query.GT:
	        return bval1 && !bval2;
	    case Query.LT:
	        return !bval1 && bval2;
	    case Query.GE:
	        return bval1 || !bval2;
	    case Query.LE:
	        return !bval1 || bval2;
	    case Query.EQ:
	        return bval1 == bval2;
	    }

	} else {
	    sval1 = ((StringValueExp)val1).getValue();
	    sval2 = ((StringValueExp)val2).getValue();

	    switch (relOp) {
	    case Query.GT:
		return sval1.compareTo(sval2) > 0;
	    case Query.LT:
		return sval1.compareTo(sval2) < 0;
	    case Query.GE:
		return sval1.compareTo(sval2) >= 0;
	    case Query.LE:
		return sval1.compareTo(sval2) <= 0;
	    case Query.EQ:
		return sval1.compareTo(sval2) == 0;
	    }
	}

	return false;
    } 
    
    /**
     * Returns the string representing the object.
     */
    public String toString()  { 
	return "(" + exp1 + ") " + relOpString() + " (" + exp2 + ")";
    } 

    private String relOpString() {
	switch (relOp) {
	case Query.GT:
	    return ">";
	case Query.LT:
	    return "<";
	case Query.GE:
	    return ">=";
	case Query.LE:
	    return "<=";
	case Query.EQ:
	    return "=";
	}
	
	return "=";
    }

 }
