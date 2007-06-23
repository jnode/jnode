/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.jndi.ldap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class VersionHelper12 extends VersionHelper {

    VersionHelper12() {} // Disallow external from creating one of these.

    ClassLoader getURLClassLoader(String[] url) 
	throws MalformedURLException {
	    ClassLoader parent = getContextClassLoader();
	    if (url != null) {
		return URLClassLoader.newInstance(getUrlArray(url), parent);
	    } else {
		return parent;
	    }
    }

    Class loadClass(String className) throws ClassNotFoundException {
	ClassLoader cl = getContextClassLoader();
	return Class.forName(className, true, cl);
    }

    private ClassLoader getContextClassLoader() {
	return (ClassLoader) AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    return Thread.currentThread().getContextClassLoader();
		}
	    }
	);
    }

    Thread createThread(final Runnable r) {
	return (Thread) AccessController.doPrivileged(
	    new PrivilegedAction() {
	        public Object run() {
		    return new Thread(r);
		}
	    }
	);
    }
}
