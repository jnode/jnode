/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.apt.mirror.declaration;


import java.util.Collection;
import java.util.ArrayList;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.tools.apt.mirror.AptEnv;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;


/**
 * Implementation of MemberDeclaration
 */

public abstract class MemberDeclarationImpl extends DeclarationImpl
					    implements MemberDeclaration {

    protected MemberDeclarationImpl(AptEnv env, Symbol sym) {
	super(env, sym);
    }


    /**
     * {@inheritDoc}
     */
    public TypeDeclaration getDeclaringType() {
	ClassSymbol c = getDeclaringClassSymbol();
	return (c == null)
	    ? null
	    : env.declMaker.getTypeDeclaration(c);
    }

    /**
     * {@inheritDoc}
     * For methods, constructors, and types.
     */
    public Collection<TypeParameterDeclaration> getFormalTypeParameters() {
	ArrayList<TypeParameterDeclaration> res =
	    new ArrayList<TypeParameterDeclaration>();
	for (Type t : sym.type.getTypeArguments()) {
	    res.add(env.declMaker.getTypeParameterDeclaration(t.tsym));
	}
	return res;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(DeclarationVisitor v) {
	v.visitMemberDeclaration(this);
    }


    /**
     * Returns the ClassSymbol of the declaring type,
     * or null if this is a top-level type.
     */
    private ClassSymbol getDeclaringClassSymbol() {
	return sym.owner.enclClass();
    }

    /**
     * Returns the formal type parameters of a type, member or constructor
     * as an angle-bracketed string.  Each parameter consists of the simple
     * type variable name and any bounds (with no implicit "extends Object"
     * clause added).  Type names are qualified.
     * Returns "" if there are no type parameters.
     */
    protected static String typeParamsToString(AptEnv env, Symbol sym) {
	if (sym.type.getTypeArguments().isEmpty()) {
	    return "";
	}
	StringBuilder s = new StringBuilder();
	for (Type t : sym.type.getTypeArguments()) {
	    Type.TypeVar tv = (Type.TypeVar) t;
	    s.append(s.length() == 0 ? "<" : ", ")
	     .append(TypeParameterDeclarationImpl.toString(env, tv));
	}
	s.append(">");
	return s.toString();
    }
}
