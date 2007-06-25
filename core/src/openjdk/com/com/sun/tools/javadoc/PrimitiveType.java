/*
 * Copyright 2001-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javadoc;

import com.sun.javadoc.*;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Type.ClassType;

class PrimitiveType implements com.sun.javadoc.Type {

    private final String name;

    static final PrimitiveType voidType = new PrimitiveType("void");
    static final PrimitiveType booleanType = new PrimitiveType("boolean");
    static final PrimitiveType byteType = new PrimitiveType("byte");
    static final PrimitiveType charType = new PrimitiveType("char");
    static final PrimitiveType shortType = new PrimitiveType("short");
    static final PrimitiveType intType = new PrimitiveType("int");
    static final PrimitiveType longType = new PrimitiveType("long");
    static final PrimitiveType floatType = new PrimitiveType("float");
    static final PrimitiveType doubleType = new PrimitiveType("double");

    // error type, should never actually be used
    static final PrimitiveType errorType = new PrimitiveType("");

    PrimitiveType(String name) {
        this.name = name;
    }

    /**
     * Return unqualified name of type excluding any dimension information.
     * <p>
     * For example, a two dimensional array of String returns 'String'.
     */
    public String typeName() {
        return name;
    }

    /**
     * Return qualified name of type excluding any dimension information.
     *<p>
     * For example, a two dimensional array of String
     * returns 'java.lang.String'.
     */
    public String qualifiedTypeName() {
        return name;
    }

    /**
     * Return the simple name of this type.
     */
    public String simpleTypeName() {
	return name;
    }

    /**
     * Return the type's dimension information, as a string.
     * <p>
     * For example, a two dimensional array of String returns '[][]'.
     */
    public String dimension() {
        return "";
    }

    /**
     * Return this type as a class.  Array dimensions are ignored.
     *
     * @return a ClassDocImpl if the type is a Class.
     * Return null if it is a primitive type..
     */
    public ClassDoc asClassDoc() {
        return null;
    }

    /**
     * Return null, as this is not an annotation type.
     */
    public AnnotationTypeDoc asAnnotationTypeDoc() {
	return null;
    }

    /**
     * Return null, as this is not an instantiation.
     */
    public ParameterizedType asParameterizedType() {
	return null;
    }

    /**
     * Return null, as this is not a type variable.
     */
    public TypeVariable asTypeVariable() {
	return null;
    }

    /**
     * Return null, as this is not a wildcard type;
     */
    public WildcardType asWildcardType() {
	return null;
    }

    /**
     * Returns a string representation of the type.
     *
     * Return name of type including any dimension information.
     * <p>
     * For example, a two dimensional array of String returns
     * <code>String[][]</code>.
     *
     * @return name of type including any dimension information.
     */
    public String toString() {
        return qualifiedTypeName();
    }

    /**
     * Return true if this is a primitive type.
     */
    public boolean isPrimitive() {
	return true;
    }
}
