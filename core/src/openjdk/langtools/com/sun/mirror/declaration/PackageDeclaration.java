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

package com.sun.mirror.declaration;


import java.util.Collection;


/**
 * Represents the declaration of a package.  Provides access to information
 * about the package and its members.
 *
 * <p> {@link com.sun.mirror.util.DeclarationFilter}
 * provides a simple way to select just the items of interest
 * when a method returns a collection of declarations.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @since 1.5
 */

public interface PackageDeclaration extends Declaration {

    /**
     * Returns the fully qualified name of this package.
     * This is also known as the package's <i>canonical</i> name.
     *
     * @return the fully qualified name of this package, or the
     * empty string if this is the unnamed package
     */
    String getQualifiedName();

    /**
     * Returns the declarations of the top-level classes in this package.
     * Interfaces are not included, but enum types are.
     *
     * @return the declarations of the top-level classes in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<ClassDeclaration> getClasses();

    /**
     * Returns the declarations of the top-level enum types in this package.
     *
     * @return the declarations of the top-level enum types in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<EnumDeclaration> getEnums();

    /**
     * Returns the declarations of the top-level interfaces in this package.
     * Annotation types are included.
     *
     * @return the declarations of the top-level interfaces in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<InterfaceDeclaration> getInterfaces();

    /**
     * Returns the declarations of the top-level annotation types in this
     * package.
     *
     * @return the declarations of the top-level annotation types in this
     * package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<AnnotationTypeDeclaration> getAnnotationTypes();
}
