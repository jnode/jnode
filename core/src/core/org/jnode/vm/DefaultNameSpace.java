/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.naming.AbstractNameSpace;
import org.jnode.vm.classmgr.VmType;

class DefaultNameSpace extends AbstractNameSpace {

    /**
     * All bound names+services
     */
    protected final Map<VmType<?>, Object> namespace = new HashMap<VmType<?>, Object>();

    /**
     * Bind a given service in the namespace under a given name.
     *
     * @param name
     * @param service
     * @throws NameAlreadyBoundException if the name already exists within this namespace
     */
    @PrivilegedActionPragma
    public <T> void bind(Class<T> name, T service) throws NamingException,
        NameAlreadyBoundException {
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        synchronized (namespace) {
            if (namespace.containsKey(VmType.fromClass(name))) {
                throw new NameAlreadyBoundException(name.getName());
            }
            namespace.put(VmType.fromClass(name), service);
        }
        
        // notify listeners
        fireServiceBound(name, service);
    }

    /**
     * Unbind a service with a given name from the namespace. If the name does
     * not exist in this namespace, this method returns without an error.
     *
     * @param name
     */
    @PrivilegedActionPragma
    public void unbind(Class<?> name) {
        final Object service;
        synchronized (namespace) {
            service = namespace.remove(VmType.fromClass((Class<?>) name));
        }
                
        // notify listeners
        fireServiceUnbound(name, service);
    }

    /**
     * Lookup a service with a given name.
     *
     * @param name
     * @throws NameNotFoundException if the name was not found in this namespace
     */
    @PrivilegedActionPragma
    public <T> T lookup(Class<T> name) throws NameNotFoundException {
        final Object result = namespace.get(VmType.fromClass(name));
        if (result == null) {
//            if (!VmIsolate.isRoot()) {
//                System.out.println("Looking for " + name.getVmClass().hashCode());
//                for (VmType<?> type : namespace.keySet()) {
//                    System.out.println("   found " + type.hashCode() + " " + (type == name.getVmClass()));
//                }
//            }
            throw new NameNotFoundException(name.getName());
        }
        return name.cast(result);
    }

    /**
     * Gets a set containing all names (Class) of the bound services.
     */
    public Set<Class<?>> nameSet() {
        final HashSet<VmType<?>> types;
        synchronized (namespace) {
            types = new HashSet<VmType<?>>(namespace.keySet());
        }
        final Set<Class<?>> result = new HashSet<Class<?>>(types.size());
        for (VmType<?> type : types) {
            result.add(type.asClass());
        }
        return result;
    }
}
