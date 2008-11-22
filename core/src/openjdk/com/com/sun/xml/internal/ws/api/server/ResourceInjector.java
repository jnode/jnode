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

package com.sun.xml.internal.ws.api.server;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.server.DefaultResourceInjector;

import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

/**
 * Represents a functionality of the container to inject resources
 * to application service endpoint object (usually but not necessarily as per JavaEE spec.)
 *
 * <p>
 * If {@link Container#getSPI(Class)} returns a valid instance of {@link ResourceInjector},
 * The JAX-WS RI will call the {@link #inject} method for each service endpoint
 * instance that it manages.
 *
 * <p>
 * The JAX-WS RI will be responsible for calling {@link PostConstruct} callback,
 * so implementations of this class need not do so.
 *
 * @author Kohsuke Kawaguchi
 * @see Container
 */
public abstract class ResourceInjector {
    /**
     * Performs resource injection.
     *
     * @param context
     *      {@link WebServiceContext} implementation to be injected into the instance.
     * @param instance
     *      Instance of the service endpoint class to which resources will be injected.
     *
     * @throws WebServiceException
     *      If the resource injection fails.
     */
    public abstract void inject(@NotNull WSWebServiceContext context, @NotNull Object instance);

    /**
     * Fallback {@link ResourceInjector} implementation used when the {@link Container}
     * doesn't provide one.
     *
     * <p>
     * Just inject {@link WSWebServiceContext} and done.
     */
    public static final ResourceInjector STANDALONE = new DefaultResourceInjector();
}
