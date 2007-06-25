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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.internal.bind.v2.model.annotation.Locatable;
import com.sun.xml.internal.bind.v2.model.core.ClassInfo;
import com.sun.xml.internal.bind.v2.model.core.ErrorHandler;
import com.sun.xml.internal.bind.v2.model.core.LeafInfo;
import com.sun.xml.internal.bind.v2.model.core.NonElement;
import com.sun.xml.internal.bind.v2.model.core.PropertyInfo;
import com.sun.xml.internal.bind.v2.model.core.PropertyKind;
import com.sun.xml.internal.bind.v2.model.core.Ref;
import com.sun.xml.internal.bind.v2.model.core.RegistryInfo;
import com.sun.xml.internal.bind.v2.model.core.TypeInfo;
import com.sun.xml.internal.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.internal.bind.v2.model.nav.Navigator;
import com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException;


/**
 * Builds a {@link TypeInfoSet} (a set of JAXB properties)
 * by using {@link ElementInfoImpl} and {@link ClassInfoImpl}.
 * from annotated Java classes.
 *
 * <p>
 * This class uses {@link Navigator} and {@link AnnotationReader} to
 * work with arbitrary annotation source and arbitrary Java model.
 * For this purpose this class is parameterized.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ModelBuilder<T,C,F,M> {

    /**
     * {@link TypeInfo}s that are built will go into this set.
     */
    final TypeInfoSetImpl<T,C,F,M> typeInfoSet;

    public final AnnotationReader<T,C,F,M> reader;

    public final Navigator<T,C,F,M> nav;

    /**
     * Used to detect collisions among global type names.
     */
    private final Map<QName,TypeInfo> typeNames = new HashMap<QName,TypeInfo>();

    /**
     * JAXB doesn't want to use namespaces unless we are told to, but WS-I BP
     * conformace requires JAX-RPC to always use a non-empty namespace URI.
     * (see http://www.ws-i.org/Profiles/BasicProfile-1.0-2004-04-16.html#WSDLTYPES R2105)
     *
     * <p>
     * To work around this issue, we allow the use of the empty namespaces to be
     * replaced by a particular designated namespace URI.
     *
     * <p>
     * This field keeps the value of that replacing namespace URI.
     * When there's no replacement, this field is set to "".
     */
    public final String defaultNsUri;

    /**
     * Packages whose registries are already added.
     */
    /*package*/ final Map<String,RegistryInfoImpl> registries = new HashMap<String,RegistryInfoImpl>();

    /**
     * @see #setErrorHandler
     */
    private ErrorHandler errorHandler;
    private boolean hadError;

    private final ErrorHandler proxyErrorHandler = new ErrorHandler() {
        public void error(IllegalAnnotationException e) {
            reportError(e);
        }
    };


    public ModelBuilder(
        AnnotationReader<T,C,F,M> reader,
        Navigator<T,C,F,M> navigator,
        String defaultNamespaceRemap ) {

        this.reader = reader;
        this.nav = navigator;
        if(defaultNamespaceRemap==null)
            defaultNamespaceRemap = "";
        this.defaultNsUri = defaultNamespaceRemap;
        reader.setErrorHandler(proxyErrorHandler);
        typeInfoSet = createTypeInfoSet();
    }

    protected TypeInfoSetImpl<T,C,F,M> createTypeInfoSet() {
        return new TypeInfoSetImpl(nav,reader,BuiltinLeafInfoImpl.createLeaves(nav));
    }

    /**
     * Builds a JAXB {@link ClassInfo} model from a given class declaration
     * and adds that to this model owner.
     *
     * <p>
     * Return type is either {@link ClassInfo} or {@link LeafInfo} (for types like
     * {@link String} or {@link Enum}-derived ones)
     */
    public NonElement<T,C> getClassInfo( C clazz, Locatable upstream ) {
        assert clazz!=null;
        NonElement<T,C> r = typeInfoSet.getClassInfo(clazz);
        if(r!=null)
            return r;

        if(nav.isEnum(clazz)) {
            EnumLeafInfoImpl<T,C,F,M> li = createEnumLeafInfo(clazz,upstream);
            typeInfoSet.add(li);
            r = li;
        } else {
            ClassInfoImpl<T,C,F,M> ci = createClassInfo(clazz,upstream);
            typeInfoSet.add(ci);

            // compute the closure by eagerly expanding references
            for( PropertyInfo<T,C> p : ci.getProperties() ) {
                if(p.kind()== PropertyKind.REFERENCE) {
                    // make sure that we have a registry for this package
                    String pkg = nav.getPackageName(ci.getClazz());
                    if(!registries.containsKey(pkg)) {
                        // insert the package's object factory
                        C c = nav.findClass(pkg + ".ObjectFactory",ci.getClazz());
                        if(c!=null)
                            addRegistry(c,(Locatable)p);
                    }
                }

                for( TypeInfo<T,C> t : p.ref() )
                    ; // just compute a reference should be suffice
            }
            ci.getBaseClass();

            r = ci;
        }

        addTypeName(r);

        return r;
    }

    /**
     * Checks the uniqueness of the type name.
     */
    private void addTypeName(NonElement<T, C> r) {
        QName t = r.getTypeName();
        if(t==null)     return;

        TypeInfo old = typeNames.put(t,r);
        if(old!=null) {
            // collision
            reportError(new IllegalAnnotationException(
                    Messages.CONFLICTING_XML_TYPE_MAPPING.format(r.getTypeName()),
                    old, r ));
        }
    }

    /**
     * Have the builder recognize the type (if it hasn't done so yet),
     * and returns a {@link NonElement} that represents it.
     *
     * @return
     *      always non-null.
     */
    public NonElement<T,C> getTypeInfo(T t,Locatable upstream) {
        NonElement<T,C> r = typeInfoSet.getTypeInfo(t);
        if(r!=null)     return r;

        if(nav.isArray(t)) { // no need for checking byte[], because above typeInfoset.getTypeInfo() would return non-null
            ArrayInfoImpl<T,C,F,M> ai =
                createArrayInfo(upstream, t);
            addTypeName(ai);
            typeInfoSet.add(ai);
            return ai;
        }

        C c = nav.asDecl(t);
        assert c!=null : t.toString()+" must be a leaf, but we failed to recognize it.";
        return getClassInfo(c,upstream);
    }

    /**
     * This method is used to add a root reference to a model.
     */
    public NonElement<T,C> getTypeInfo(Ref<T,C> ref) {
        // TODO: handle XmlValueList
        assert !ref.valueList;
        C c = nav.asDecl(ref.type);
        if(c!=null && reader.getClassAnnotation(XmlRegistry.class,c,null/*TODO: is this right?*/)!=null) {
            if(!registries.containsKey(nav.getPackageName(c)))
                addRegistry(c,null);
            return null;    // TODO: is this correct?
        } else
            return getTypeInfo(ref.type,null);
    }


    protected EnumLeafInfoImpl<T,C,F,M> createEnumLeafInfo(C clazz,Locatable upstream) {
        return new EnumLeafInfoImpl<T,C,F,M>(this,upstream,clazz,nav.use(clazz));
    }

    protected ClassInfoImpl<T,C,F,M> createClassInfo(
            C clazz, Locatable upstream ) {
        return new ClassInfoImpl<T,C,F,M>(this,upstream,clazz);
    }

    protected ElementInfoImpl<T,C,F,M> createElementInfo(
        RegistryInfoImpl<T,C,F,M> registryInfo, M m) throws IllegalAnnotationException {
        return new ElementInfoImpl<T,C,F,M>(this,registryInfo,m);
    }

    protected ArrayInfoImpl<T,C,F,M> createArrayInfo(Locatable upstream, T arrayType) {
        return new ArrayInfoImpl<T, C, F, M>(this,upstream,arrayType);
    }


    /**
     * Visits a class with {@link XmlRegistry} and records all the element mappings
     * in it.
     */
    public RegistryInfo<T,C> addRegistry(C registryClass, Locatable upstream ) {
        RegistryInfoImpl<T,C,F,M> r = new RegistryInfoImpl<T,C,F,M>(this,upstream,registryClass);
        return r;
    }

    /**
     * Gets a {@link RegistryInfo} for the given package.
     *
     * @return
     *      null if no registry exists for the package.
     *      unlike other getXXX methods on this class,
     *      this method is side-effect free.
     */
    public RegistryInfo<T,C> getRegistry(String packageName) {
        return registries.get(packageName);
    }

    private boolean linked;

    /**
     * Called after all the classes are added to the type set
     * to "link" them together.
     *
     * <p>
     * Don't expose implementation classes in the signature.
     *
     * @return
     *      fully built {@link TypeInfoSet} that represents the model,
     *      or null if there was an error.
     */
    public TypeInfoSet<T,C,F,M> link() {

        assert !linked;
        linked = true;

        for( ElementInfoImpl ei : typeInfoSet.getAllElements() )
            ei.link();

        for( ClassInfoImpl ci : typeInfoSet.beans().values() )
            ci.link();

        for( EnumLeafInfoImpl li : typeInfoSet.enums().values() )
            li.link();

        if(hadError)
            return null;
        else
            return typeInfoSet;
    }

//
//
// error handling
//
//

    /**
     * Sets the error handler that receives errors discovered during the model building.
     *
     * @param errorHandler
     *      can be null.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public final void reportError(IllegalAnnotationException e) {
        hadError = true;
        if(errorHandler!=null)
            errorHandler.error(e);
    }
}
