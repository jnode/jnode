/*
 * Copyright 1999-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.naming.internal;

import java.util.List;
import javax.naming.NamingException;

/**
  * The FactoryEnumeration is used for returning factory instances.
  * 
  * @author Rosanna Lee
  * @author Scott Seligman
 */

// no need to implement Enumeration since this is only for internal use
public final class FactoryEnumeration { 
    private List factories;
    private int posn = 0;
    private ClassLoader loader;

    /**
     * Records the input list and uses it directly to satisfy
     * hasMore()/next() requests. An alternative would have been to use
     * an enumeration/iterator from the list, but we want to update the 
     * list so we keep the
     * original list. The list initially contains Class objects.
     * As each element is used, the Class object is replaced by an
     * instance of the Class itself; eventually, the list contains
     * only a list of factory instances and no more updates are required.
     *
     * <p> Both Class objects and factories are wrapped in weak
     * references so as not to prevent GC of the class loader.  Each
     * weak reference is tagged with the factory's class name so the
     * class can be reloaded if the reference is cleared.

     * @param factories	A non-null list
     * @param loader	The class loader of the list's contents
     */
    FactoryEnumeration(List factories, ClassLoader loader) {
	this.factories = factories;
	this.loader = loader;
    }
 
    public Object next() throws NamingException {
	synchronized (factories) {

	    NamedWeakReference ref = (NamedWeakReference) factories.get(posn++);
	    Object answer = ref.get();
	    if ((answer != null) && !(answer instanceof Class)) {
		return answer;
	    }

	    String className = ref.getName();

	    try {
		if (answer == null) {	// reload class if weak ref cleared
		    answer = Class.forName(className, true, loader);
		}
		// Instantiate Class to get factory
		answer = ((Class) answer).newInstance();
		ref = new NamedWeakReference(answer, className);
		factories.set(posn-1, ref);  // replace Class object or null
		return answer;
	    } catch (ClassNotFoundException e) {
		NamingException ne = 
		    new NamingException("No longer able to load " + className);
		ne.setRootCause(e);
		throw ne;
	    } catch (InstantiationException e) {
		NamingException ne = 
		    new NamingException("Cannot instantiate " + answer);
		ne.setRootCause(e);
		throw ne;
	    } catch (IllegalAccessException e) {
		NamingException ne = new NamingException("Cannot access " + answer);
		ne.setRootCause(e);
		throw ne;
	    }
	}
    }

    public boolean hasMore() {
	synchronized (factories) {
	    return posn < factories.size();
	}
    }
}
