/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.xml.bind;
import  javax.xml.namespace.QName;

/**
 * Provide access to JAXB xml binding data for a JAXB object.
 *
 * <p>
 * Intially, the intent of this class is to just conceptualize how 
 * a JAXB application developer can access xml binding information, 
 * independent if binding model is java to schema or schema to java.
 * Since accessing the XML element name related to a JAXB element is
 * a highly requested feature, demonstrate access to this
 * binding information.
 *
 * The factory method to get a <code>JAXBIntrospector</code> instance is 
 * {@link JAXBContext#createJAXBIntrospector()}.
 *
 * @see JAXBContext#createJAXBIntrospector()
 * @since JAXB2.0
 */
public abstract class JAXBIntrospector {

    /** 
     * <p>Return true iff <code>object</code> represents a JAXB element.</p>
     * <p>Parameter <code>object</code> is a JAXB element for following cases:
     * <ol>
     *   <li>It is an instance of <code>javax.xml.bind.JAXBElement</code>.</li>
     *   <li>The class of <code>object</code> is annotated with 
     *       <code>&#64XmlRootElement</code>.
     *   </li>
     * </ol>
     *
     * @see #getElementName(Object)
     */
    public abstract boolean isElement(Object object);

    /**
     * <p>Get xml element qname for <code>jaxbElement</code>.</p>
     *
     * @param jaxbElement is an object that {@link #isElement(Object)} returned true.
     *                    
     * @return xml element qname associated with jaxbElement;
     *         null if <code>jaxbElement</code> is not a JAXB Element.
     */
    public abstract QName getElementName(Object jaxbElement);

    /**
     * <p>Get the element value of a JAXB element.</p>
     *
     * <p>Convenience method to abstract whether working with either 
     *    a javax.xml.bind.JAXBElement instance or an instance of 
     *    <tt>&#64XmlRootElement</tt> annotated Java class.</p>
     *
     * @param jaxbElement  object that #isElement(Object) returns true.
     *
     * @return The element value of the <code>jaxbElement</code>.
     */
    public static Object getValue(Object jaxbElement) {
	if (jaxbElement instanceof JAXBElement) {
	    return ((JAXBElement)jaxbElement).getValue();
	} else {
	    // assume that class of this instance is 
	    // annotated with @XmlRootElement.
	    return jaxbElement;
	}
    }
}
