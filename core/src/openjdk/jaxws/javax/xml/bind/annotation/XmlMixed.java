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

package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import org.w3c.dom.Element;
import javax.xml.bind.JAXBElement;

/**
 * <p>
 * Annotate a JavaBean multi-valued property to support mixed content.
 *
 * <p>
 * The usage is subject to the following constraints:
 * <ul>
 *   <li> can be used with &#64;XmlElementRef, &#64;XmlElementRefs or &#64;XmlAnyElement</li>
 * </ul>
 * <p>
 * The following can be inserted into &#64;XmlMixed annotated multi-valued property
 * <ul>
 * <li>XML text information items are added as values of java.lang.String.</li>
 * <li>Children element information items are added as instances of
 * {@link JAXBElement} or instances with a class that is annotated with
 * &#64;XmlRootElement.</li>
 * <li>Unknown content that is not be bound to a JAXB mapped class is inserted
 * as {@link Element}. (Assumes property annotated with &#64;XmlAnyElement)</li>
 * </ul>
 *
 * Below is an example of binding and creation of mixed content.
 * <pre><xmp>
 *  <!-- schema fragment having  mixed content -->
 *  <xs:complexType name="letterBody" mixed="true">
 *    <xs:sequence>
 *	<xs:element name="name" type="xs:string"/>
 *	<xs:element name="quantity" type="xs:positiveInteger"/>
 *	<xs:element name="productName" type="xs:string"/>
 *	<!-- etc. -->
 *    </xs:sequence>
 *  </xs:complexType>
 *  <xs:element name="letterBody" type="letterBody"/>
 * 
 * // Schema-derived Java code: 
 * // (Only annotations relevant to mixed content are shown below, 
 * //  others are ommitted.)
 * import java.math.BigInteger;
 * public class ObjectFactory {
 * 	// element instance factories
 * 	JAXBElement<LetterBody> createLetterBody(LetterBody value);
 * 	JAXBElement<String>     createLetterBodyName(String value);
 * 	JAXBElement<BigInteger> createLetterBodyQuantity(BigInteger value);
 * 	JAXBElement<String>     createLetterBodyProductName(String value);
 *      // type instance factory
 * 	LetterBody> createLetterBody();
 * }
 * </xmp></pre>
 * <pre>
 * public class LetterBody {
 * 	// Mixed content can contain instances of Element classes
 * 	// Name, Quantity and ProductName. Text data is represented as
 *	// java.util.String for text.
 *	&#64;XmlMixed 
 * 	&#64;XmlElementRefs({
 *		&#64;XmlElementRef(name="productName", type=JAXBElement.class),
 *		&#64;XmlElementRef(name="quantity", type=JAXBElement.class),
 *		&#64;XmlElementRef(name="name", type=JAXBElement.class)})
 *	List getContent(){...}
 * }
 * </pre>
 * The following is an XML instance document with mixed content
 * <pre><xmp>
 * <letterBody>
 * Dear Mr.<name>Robert Smith</name>
 * Your order of <quantity>1</quantity> <productName>Baby
 * Monitor</productName> shipped from our warehouse. ....
 * </letterBody>
 * </xmp></pre>
 * that can be constructed using following JAXB API calls.
 * <pre><xmp>
 * LetterBody lb = ObjectFactory.createLetterBody();
 * JAXBElement<LetterBody> lbe = ObjectFactory.createLetterBody(lb);
 * List gcl = lb.getContent();  //add mixed content to general content property.
 * gcl.add("Dear Mr.");  // add text information item as a String.
 * 
 * // add child element information item
 * gcl.add(ObjectFactory.createLetterBodyName("Robert Smith"));
 * gcl.add("Your order of "); // add text information item as a String
 * 
 * // add children element information items
 * gcl.add(ObjectFactory.
 * 	 		createLetterBodyQuantity(new BigInteger("1")));
 * gcl.add(ObjectFactory.createLetterBodyProductName("Baby Monitor"));
 * gcl.add("shipped from our warehouse");  // add text information item
 * </xmp></pre>
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 * @author Kohsuke Kawaguchi
 * @since JAXB2.0
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlMixed {
}
