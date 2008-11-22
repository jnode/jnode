/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

import static com.sun.javadoc.LanguageVersion.*;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;


/**
 * Implementation of <code>WildcardType</code>, which 
 * represents a wildcard type.
 *
 * @author Scott Seligman
 * @since 1.5
 */
public class WildcardTypeImpl extends AbstractTypeImpl implements WildcardType {

    WildcardTypeImpl(DocEnv env, Type.WildcardType type) {
	super(env, type);
    }

    /**
     * Return the upper bounds of this wildcard type argument
     * as given by the <i>extends</i> clause.
     * Return an empty array if no such bounds are explicitly given.
     */
    public com.sun.javadoc.Type[] extendsBounds() {
	return TypeMaker.getTypes(env, getExtendsBounds((Type.WildcardType)type));
    }

    /**
     * Return the lower bounds of this wildcard type argument
     * as given by the <i>super</i> clause.
     * Return an empty array if no such bounds are explicitly given.
     */
    public com.sun.javadoc.Type[] superBounds() {
	return TypeMaker.getTypes(env, getSuperBounds((Type.WildcardType)type));
    }

    /**
     * Return the ClassDoc of the erasure of this wildcard type.
     */
    public ClassDoc asClassDoc() {
	return env.getClassDoc((ClassSymbol)env.types.erasure(type).tsym);
    }

    public WildcardType asWildcardType() {
	return this;
    }

    public String typeName()		{ return "?"; }
    public String qualifiedTypeName()	{ return "?"; }
    public String simpleTypeName()	{ return "?"; }

    public String toString() {
	return wildcardTypeToString(env, (Type.WildcardType)type, true);
    }


    /**
     * Return the string form of a wildcard type ("?") along with any
     * "extends" or "super" clause.  Delimiting brackets are not
     * included.  Class names are qualified if "full" is true.
     */
    static String wildcardTypeToString(DocEnv env,
				       Type.WildcardType wildThing, boolean full) {
	if (env.legacyDoclet) {
	    return TypeMaker.getTypeName(env.types.erasure(wildThing), full);
	}
	StringBuffer s = new StringBuffer("?");
	List<Type> bounds = getExtendsBounds(wildThing);
	if (bounds.nonEmpty()) {
	    s.append(" extends ");
	} else {
	    bounds = getSuperBounds(wildThing);
	    if (bounds.nonEmpty()) {
		s.append(" super ");
	    }
	}
	boolean first = true;	// currently only one bound is allowed
	for (Type b : bounds) {
	    if (!first) {
		s.append(" & ");
	    }
	    s.append(TypeMaker.getTypeString(env, b, full));
	    first = false;
	}
	return s.toString();
    }

    private static List<Type> getExtendsBounds(Type.WildcardType wild) {
	return wild.isSuperBound()
		? List.<Type>nil()
		: List.of(wild.type);
    }

    private static List<Type> getSuperBounds(Type.WildcardType wild) {
	return wild.isExtendsBound()
		? List.<Type>nil()
		: List.of(wild.type);
    }
}
