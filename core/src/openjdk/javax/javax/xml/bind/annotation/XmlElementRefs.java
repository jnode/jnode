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

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a property that refers to classes with {@link XmlElement}
 * or JAXBElement.
 *
 * <p>
 * Compared to an element property (property with {@link XmlElement}
 * annotation), a reference property has a different substitution semantics.
 * When a sub-class is assigned to a property, an element property produces
 * the same tag name with @xsi:type, whereas a reference property produces
 * a different tag name (the tag name that's on the the sub-class.)
 *
 * <p> This annotation can be used with the following annotations: 
 * {@link XmlJavaTypeAdapter}, {@link XmlElementWrapper}.
 *
 * @author <ul><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Sekhar Vajjhala, Sun Microsystems, Inc.</li></ul>
 *
 * @see XmlElementWrapper
 * @see XmlElementRef
 * @since JAXB2.0
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlElementRefs {
    XmlElementRef[] value();
}
