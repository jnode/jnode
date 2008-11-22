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

package com.sun.xml.internal.ws.transport.http.client;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.api.PropertySet;
import com.sun.xml.internal.ws.client.ResponseContext;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * Properties exposed from {@link HttpTransportPipe} for {@link ResponseContext}.
 *
 * @author Kohsuke Kawaguchi
 */
final class HttpResponseProperties extends PropertySet {

    private final HttpClientTransport deferedCon;

    public HttpResponseProperties(@NotNull HttpClientTransport con) {
        this.deferedCon = con;
    }

    @Property(MessageContext.HTTP_RESPONSE_HEADERS)
    public Map<String, List<String>> getResponseHeaders() {
        return deferedCon.getHeaders();
    }

    @Property(MessageContext.HTTP_RESPONSE_CODE)
    public int getResponseCode() {
        return deferedCon.statusCode;
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
        
    private static final PropertyMap model;

    static {
        model = parse(HttpResponseProperties.class);
    }
}
