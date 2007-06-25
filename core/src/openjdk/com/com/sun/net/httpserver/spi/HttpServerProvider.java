/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.net.httpserver.spi;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import sun.misc.Service;
import sun.misc.ServiceConfigurationError;
import sun.security.action.GetPropertyAction;
import com.sun.net.httpserver.*;

/**
 * Service provider class for HttpServer.
 * Sub-classes of HttpServerProvider provide an implementation of {@link HttpServer} and
 * associated classes. Applications do not normally use this class. 
 * See {@link #provider()} for how providers are found and loaded.
 */
public abstract class HttpServerProvider {

    /**
     * creates a HttpServer from this provider
     * @param addr the address to bind to. May be <code>null</code>
     * @param backlog the socket backlog. A value of <code>zero</code> means the systems default
     */
    public abstract HttpServer createHttpServer (InetSocketAddress addr, int backlog) throws IOException;

    /**
     * creates a HttpsServer from this provider
     * @param addr the address to bind to. May be <code>null</code>
     * @param backlog the socket backlog. A value of <code>zero</code> means the systems default
     */
    public abstract HttpsServer createHttpsServer (InetSocketAddress addr, int backlog) throws IOException;



    private static final Object lock = new Object();
    private static HttpServerProvider provider = null;

    /**
     * Initializes a new instance of this class.  </p>
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("httpServerProvider")</tt>
     */
    protected HttpServerProvider() {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null)
	    sm.checkPermission(new RuntimePermission("httpServerProvider"));
    }

    private static boolean loadProviderFromProperty() {
	String cn = System.getProperty("com.sun.net.httpserver.HttpServerProvider");
	if (cn == null)
	    return false;
	try {
	    Class c = Class.forName(cn, true,
				    ClassLoader.getSystemClassLoader());
	    provider = (HttpServerProvider)c.newInstance();
	    return true;
	} catch (ClassNotFoundException x) {
	    throw new ServiceConfigurationError(x);
	} catch (IllegalAccessException x) {
	    throw new ServiceConfigurationError(x);
	} catch (InstantiationException x) {
	    throw new ServiceConfigurationError(x);
	} catch (SecurityException x) {
	    throw new ServiceConfigurationError(x);
	}
    }

    private static boolean loadProviderAsService() {
	Iterator i = Service.providers(HttpServerProvider.class,
				       ClassLoader.getSystemClassLoader());
	for (;;) {
	    try {
		if (!i.hasNext())
		    return false;
		provider = (HttpServerProvider)i.next();
		return true;
	    } catch (ServiceConfigurationError sce) {
		if (sce.getCause() instanceof SecurityException) {
		    // Ignore the security exception, try the next provider
		    continue;
		}
		throw sce;
	    }
	}
    }

    /**
     * Returns the system wide default HttpServerProvider for this invocation of
     * the Java virtual machine.
     *
     * <p> The first invocation of this method locates the default provider
     * object as follows: </p>
     *
     * <ol>
     *
     *   <li><p> If the system property
     *   <tt>com.sun.net.httpserver.HttpServerProvider</tt> is defined then it is
     *   taken to be the fully-qualified name of a concrete provider class.
     *   The class is loaded and instantiated; if this process fails then an
     *   unspecified unchecked error or exception is thrown.  </p></li>
     *
     *   <li><p> If a provider class has been installed in a jar file that is
     *   visible to the system class loader, and that jar file contains a
     *   provider-configuration file named
     *   <tt>com.sun.net.httpserver.HttpServerProvider</tt> in the resource
     *   directory <tt>META-INF/services</tt>, then the first class name
     *   specified in that file is taken.  The class is loaded and
     *   instantiated; if this process fails then an unspecified unchecked error or exception is
     *   thrown.  </p></li>
     *
     *   <li><p> Finally, if no provider has been specified by any of the above
     *   means then the system-default provider class is instantiated and the
     *   result is returned.  </p></li>
     *
     * </ol>
     *
     * <p> Subsequent invocations of this method return the provider that was
     * returned by the first invocation.  </p>
     *
     * @return  The system-wide default HttpServerProvider
     */
    public static HttpServerProvider provider () {
        //jnode
        throw new UnsupportedOperationException();
        /*
    synchronized (lock) {
        if (provider != null)
		return provider;
	    return (HttpServerProvider)AccessController
		.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
			    if (loadProviderFromProperty())
				return provider;
			    if (loadProviderAsService())
				return provider;
			    provider = new sun.net.httpserver.DefaultHttpServerProvider();
			    return provider;
			}
		    });
	}
	*/
    }

}
