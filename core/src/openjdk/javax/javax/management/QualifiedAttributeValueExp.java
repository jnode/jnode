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
 * This class represents indexed attributes used as arguments to relational
 * constraints. An QualifiedAttributeValueExp may be used anywhere a
 * ValueExp is required.
 * @serial include
 *
 * @since 1.5
 */
class QualifiedAttributeValueExp extends AttributeValueExp   { 


    /* Serial version */
    private static final long serialVersionUID = 8832517277410933254L;

    /**
     * @serial The attribute class name
     */
    private String className;
        

    /**
     * Basic Constructor.
     */
    public QualifiedAttributeValueExp() { 
    } 

    /**
     * Creates a new QualifiedAttributeValueExp representing the specified object
     * attribute, named attr with class name className.
     */
    public QualifiedAttributeValueExp(String className, String attr) { 
	super(attr);
	this.className = className;
    } 


    /**
     * Returns a string representation of the class name of the attribute.
     */
    public String getAttrClassName()  { 
	return className;
    } 

    /**
     * Applies the QualifiedAttributeValueExp to an MBean.
     *
     * @param name The name of the MBean on which the QualifiedAttributeValueExp will be applied.
     *
     * @return  The ValueExp.
     *
     * @exception BadStringOperationException
     * @exception BadBinaryOpValueExpException
     * @exception BadAttributeValueExpException 
     * @exception InvalidApplicationException
     */
    public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
	BadAttributeValueExpException, InvalidApplicationException  {
	try {
	    MBeanServer server = QueryEval.getMBeanServer();
	    String v = server.getObjectInstance(name).getClassName();
	    
	    if (v.equals(className)) {
		return super.apply(name);
	    }
	    throw new InvalidApplicationException("Class name is " + v +
						  ", should be " + className);
	    
	} catch (Exception e) {
	    throw new InvalidApplicationException("Qualified attribute: " + e);
	    /* Can happen if MBean disappears between the time we
	       construct the list of MBeans to query and the time we
	       evaluate the query on this MBean, or if
	       getObjectInstance throws SecurityException.  */
	}
    } 
    
    /**
     * Returns the string representing its value
     */        
    public String toString()  { 
	if (className != null) {
	    return className + "." + super.toString();
	} else {
	    return super.toString();
	}
    } 
    
}
