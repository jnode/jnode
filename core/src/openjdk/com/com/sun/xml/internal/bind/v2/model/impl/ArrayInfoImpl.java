/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.xml.internal.bind.v2.model.impl;

import javax.xml.namespace.QName;

import com.sun.xml.internal.bind.v2.TODO;
import com.sun.xml.internal.bind.v2.WellKnownNamespace;
import com.sun.xml.internal.bind.v2.model.annotation.Locatable;
import com.sun.xml.internal.bind.v2.model.core.ArrayInfo;
import com.sun.xml.internal.bind.v2.model.core.NonElement;
import com.sun.xml.internal.bind.v2.runtime.Location;

/**
 *
 * <p>
 * Public because XJC needs to access it
 *
 * @author Kohsuke Kawaguchi
 */
public class ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ArrayInfo<TypeT,ClassDeclT>, Location {

    private final NonElement<TypeT,ClassDeclT> itemType;

    private final QName typeName;

    /**
     * The representation of T[] in the underlying reflection library.
     */
    private final TypeT arrayType;

    public ArrayInfoImpl(ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder,
                         Locatable upstream, TypeT arrayType) {
        super(builder, upstream);
        this.arrayType = arrayType;
        this.itemType = builder.getTypeInfo(nav().getComponentType(arrayType), this);

        // TODO: check itemType.getTypeName()!=null and report an error
        QName n = itemType.getTypeName();
        this.typeName = calcArrayTypeName(n);
    }

    /**
     * Computes the type name of the array from that of the item type.
     */
    public static final QName calcArrayTypeName(QName n) {
        String uri;
        if(n.getNamespaceURI().equals(WellKnownNamespace.XML_SCHEMA)) {
            TODO.checkSpec("this URI");
            uri = "http://jaxb.dev.java.net/array";
        } else
            uri = n.getNamespaceURI();
        return new QName(uri,n.getLocalPart()+"Array");
    }

    public NonElement<TypeT, ClassDeclT> getItemType() {
        return itemType;
    }

    public QName getTypeName() {
        return typeName;
    }

    public boolean isSimpleType() {
        return false;
    }

    public TypeT getType() {
        return arrayType;
    }

    /**
     * Leaf-type cannot be referenced from IDREF.
     *
     * @deprecated
     *      why are you calling a method whose return value is always known?
     */
    public final boolean canBeReferencedByIDREF() {
        return false;
    }

    public Location getLocation() {
        return this;
    }
    public String toString() {
        return nav().getTypeName(arrayType);
    }
}
