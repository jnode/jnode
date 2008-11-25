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
package com.sun.xml.internal.xsom.impl;

import com.sun.xml.internal.xsom.XSAnnotation;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class AnnotationImpl implements XSAnnotation
{
    private Object annotation;
    public Object getAnnotation() { return annotation; }
    
    public Object setAnnotation(Object o) {
        Object r = this.annotation;
        this.annotation = o;
        return r;
    }

    private final Locator locator;
    public Locator getLocator() { return locator; }
    
    public AnnotationImpl( Object o, Locator _loc ) {
        this.annotation = o;
        this.locator = _loc;
    }

    public AnnotationImpl() {
        locator = NULL_LOCATION;
    }


    private static final LocatorImpl NULL_LOCATION = new LocatorImpl();
}
