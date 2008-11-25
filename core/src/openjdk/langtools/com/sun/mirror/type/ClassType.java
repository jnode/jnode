/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.mirror.type;


import com.sun.mirror.declaration.*;


/**
 * Represents a class type.
 * Interface types are represented separately by {@link InterfaceType}.
 * Note that an {@linkplain EnumType enum} is a kind of class.
 *
 * <p> While a {@link ClassDeclaration} represents the <i>declaration</i>
 * of a class, a <tt>ClassType</tt> represents a class <i>type</i>.
 * See {@link TypeDeclaration} for more on this distinction.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @since 1.5
 */

public interface ClassType extends DeclaredType {

    /**
     * {@inheritDoc}
     */
    ClassDeclaration getDeclaration();

    /**
     * Returns the class type that is a direct supertype of this one.
     * This is the superclass of this type's declaring class, with any
     * type arguments substituted in.
     * The only class with no superclass is <tt>java.lang.Object</tt>,
     * for which this method returns <tt>null</tt>.
     *
     * <p> For example, the class type extended by
     * {@code java.util.TreeSet<String>} is
     * {@code java.util.AbstractSet<String>}.
     *
     * @return the class type that is a direct supertype of this one,
     * or <tt>null</tt> if there is none
     */
    ClassType getSuperclass();
}
