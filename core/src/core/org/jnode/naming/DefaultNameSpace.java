/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.naming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

public class DefaultNameSpace implements NameSpace {

    /** All bound names+services */
    protected final Map<Class<?>, Object> namespace = new HashMap<Class<?>, Object>();

    /**
     * Bind a given service in the namespace under a given name.
     * 
     * @param name
     * @param service
     * @throws NameAlreadyBoundException
     *             if the name already exists within this namespace
     */
    public <T> void bind(Class<T> name, T service) throws NamingException,
            NameAlreadyBoundException {
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        synchronized (namespace) {
            if (namespace.containsKey(name)) {
                throw new NameAlreadyBoundException(name.getName());
            }
            namespace.put(name, service);
        }
    }

    /**
     * Unbind a service with a given name from the namespace. If the name does
     * not exist in this namespace, this method returns without an error.
     * 
     * @param name
     */
    public void unbind(Class<?> name) {
        synchronized (namespace) {
            namespace.remove(name);
        }
    }

    /**
     * Lookup a service with a given name.
     * 
     * @param name
     * @throws NameNotFoundException
     *             if the name was not found in this namespace
     */
    public <T> T lookup(Class<T> name) throws NameNotFoundException {
        final Object result = namespace.get(name);
        if (result == null) {
            throw new NameNotFoundException(name.getName());
        }
        return name.cast(result);
    }

    /**
     * Gets a set containing all names (Class) of the bound services.
     */
    public Set<Class<?>> nameSet() {
        final Set<Class<?>> result;
        synchronized (namespace) {
            result = new HashSet<Class<?>>(namespace.keySet());
        }
        return result;
    }
}
