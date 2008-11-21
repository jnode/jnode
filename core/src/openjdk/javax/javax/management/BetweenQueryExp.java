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
 * relations.
 * @serial include
 *
 * @since 1.5
 */
class BetweenQueryExp extends QueryEval implements QueryExp { 

    /* Serial version */
    private static final long serialVersionUID = -2933597532866307444L;
    
    /** 
     * @serial The checked value 
     */
    private ValueExp exp1;

    /** 
     * @serial The lower bound value 
     */
    private ValueExp exp2;

    /** 
     * @serial The upper bound value 
     */
    private ValueExp exp3;
    
    
    /**
     * Basic Constructor.
     */
    public BetweenQueryExp() { 
    } 
    
    /**
     * Creates a new BetweenQueryExp with v1 checked value, v2 lower bound
     * and v3 upper bound values.
     */
    public BetweenQueryExp(ValueExp v1, ValueExp v2, ValueExp v3) { 
	exp1  = v1;
	exp2  = v2;
	exp3  = v3;
    } 


    /**
     * Returns the checked value of the query.
     */
    public ValueExp getCheckedValue()  { 
	return exp1;
    } 

    /**
     * Returns the lower bound value of the query.
     */
    public ValueExp getLowerBound()  { 
	return exp2;
    } 

    /**
     * Returns the upper bound value of the query.
     */    
    public ValueExp getUpperBound()  { 
	return exp3;
    } 

    /**
     * Applies the BetweenQueryExp on an MBean.
     *
     * @param name The name of the MBean on which the BetweenQueryExp will be applied.
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
	ValueExp val1 = exp1.apply(name);
	ValueExp val2 = exp2.apply(name);
	ValueExp val3 = exp3.apply(name);
	String sval1;
	String sval2;
	String sval3;
	double dval1;
	double dval2;
	double dval3;
	long lval1;
	long lval2;
	long lval3;
	boolean numeric = val1 instanceof NumericValueExp;
	
	if (numeric) {
	    if (((NumericValueExp)val1).isLong()) {
		lval1 = ((NumericValueExp)val1).longValue();
		lval2 = ((NumericValueExp)val2).longValue();
		lval3 = ((NumericValueExp)val3).longValue();
		return lval2 <= lval1 && lval1 <= lval3;
	    } else {
		dval1 = ((NumericValueExp)val1).doubleValue();
		dval2 = ((NumericValueExp)val2).doubleValue();
		dval3 = ((NumericValueExp)val3).doubleValue();
		return dval2 <= dval1 && dval1 <= dval3;
	    }
	    
	} else {
	    sval1 = ((StringValueExp)val1).toString();
	    sval2 = ((StringValueExp)val2).toString();
	    sval3 = ((StringValueExp)val3).toString();
	    return sval2.compareTo(sval1) <= 0 && sval1.compareTo(sval3) <= 0;
	}
    } 
  
    /**
     * Returns the string representing the object.
     */   
    public String toString()  { 
	return "(" + exp1 + ") between (" + exp2 + ") and (" + exp3 + ")";
    } 

 }
