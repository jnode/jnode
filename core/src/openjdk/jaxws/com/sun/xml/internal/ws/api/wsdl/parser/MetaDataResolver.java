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

package com.sun.xml.internal.ws.api.wsdl.parser;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.net.URI;

/**
 * Resolves metadata such as WSDL/schema. This serves as extensibile plugin point which a wsdl parser can use to
 * get the metadata from an endpoint.
 *
 * @author Vivek Pandey
 */
public abstract class MetaDataResolver {
    /**
     * Gives {@link com.sun.xml.internal.ws.api.wsdl.parser.ServiceDescriptor} resolved from the given location.
     *
     * TODO: Does this method need to propogate errors?
     *
     * @param location metadata location
     * @return {@link com.sun.xml.internal.ws.api.wsdl.parser.ServiceDescriptor} resolved from the location. It may be null in the cases when MetadataResolver
     *         can get the metada associated with the metadata loction.
     */
    public abstract @Nullable ServiceDescriptor resolve(@NotNull URI location);
}
