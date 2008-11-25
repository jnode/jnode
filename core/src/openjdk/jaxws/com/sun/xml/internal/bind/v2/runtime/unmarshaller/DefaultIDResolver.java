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
package com.sun.xml.internal.bind.v2.runtime.unmarshaller;

import java.util.HashMap;
import java.util.concurrent.Callable;

import com.sun.xml.internal.bind.IDResolver;

/**
 * Default implementation of {@link IDResolver}.
 *
 * @author Kohsuke Kawaguchi
 */
final class DefaultIDResolver extends IDResolver {
    /** Records ID->Object map. */
    private HashMap<String,Object> idmap = null;

    public void startDocument() {
        if(idmap!=null)
            idmap.clear();
    }

    public void bind(String id, Object obj) {
        if(idmap==null)     idmap = new HashMap<String,Object>();
        idmap.put(id,obj);
    }

    public Callable resolve(final String id, Class targetType) {
        return new Callable() {
            public Object call() throws Exception {
                if(idmap==null)     return null;
                return idmap.get(id);
            }
        };
    }
}
