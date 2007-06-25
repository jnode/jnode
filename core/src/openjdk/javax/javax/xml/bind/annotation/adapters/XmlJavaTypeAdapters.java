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

package javax.xml.bind.annotation.adapters;

import static java.lang.annotation.ElementType.PACKAGE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * <p>
 * A container for multiple @{@link XmlJavaTypeAdapter} annotations.
 *
 * <p> Multiple annotations of the same type are not allowed on a program
 * element. This annotation therefore serves as a container annotation
 * for multiple &#64;XmlJavaTypeAdapter as follows:
 *
 * <pre>
 * &#64;XmlJavaTypeAdapters ({ @XmlJavaTypeAdapter(...),@XmlJavaTypeAdapter(...) })
 * </pre>
 *
 * <p>The <tt>@XmlJavaTypeAdapters</tt> annnotation is useful for
 * defining {@link XmlJavaTypeAdapter} annotations for different types
 * at the package level.
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 *
 * @author <ul><li>Sekhar Vajjhala, Sun Microsystems, Inc.</li></ul>
 * @see XmlJavaTypeAdapter
 * @since JAXB2.0
 */
@Retention(RUNTIME) @Target({PACKAGE})
public @interface XmlJavaTypeAdapters {
    /**
     * Collection of @{@link XmlJavaTypeAdapter} annotations
     */
    XmlJavaTypeAdapter[] value();
}
