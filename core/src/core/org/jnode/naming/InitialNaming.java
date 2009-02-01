/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.naming;

import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.vm.annotation.SharedStatics;

/**
 * This class provides a namespace that is used by the JNode system. Various
 * services are bound into this namespace. <p/>A service bound into this
 * namespace can be any object. Is does not have to implement any particular
 * interface. It is up to the user of this namespace to cast the objects as
 * needed. <p/>Only a single service can be bound under each name. There is no
 * restriction on the syntax of a name. Nor is there any interpretation of a
 * name by this namespace.
 *
 * @author epr
 */
@SharedStatics
public final class InitialNaming {

    /**
     * All bound names+services
     */
    private static NameSpace namespace;

    /**
     * Add a {@link NameSpaceListener} to the NameSpace
     * @param <T>
     * @param name
     * @param l
     */
    public static <T> void addNameSpaceListener(Class<T> name, NameSpaceListener<T> l) {
        getNameSpace().addNameSpaceListener(name, l);
    }
    
    /**
     * Remove a {@link NameSpaceListener} from the NameSpace
     * @param <T>
     * @param name
     * @param l
     */
    public static <T> void removeNameSpaceListener(Class<T> name, NameSpaceListener<T> l) {
        getNameSpace().removeNameSpaceListener(name, l);        
    }

    /**
     * Define the {@link NameSpace} used for storing services.
     * If a {@link NameSpace} is already defined, then a {@link SecurityException} is thrown.
     * @param namespace
     */
    public static void setNameSpace(NameSpace namespace) {
        if (InitialNaming.namespace != null) {
            throw new SecurityException(
                "namespace can't be modified after first initialization");
        }
        InitialNaming.namespace = namespace;
    }

    /**
     * Bind a given service in the namespace under a given name.
     *
     * @param name
     * @param service
     * @throws NameAlreadyBoundException if the name already exists within this namespace
     */
    public static <T, E extends T> void bind(Class<T> name, E service) throws NamingException,
        NameAlreadyBoundException {
        getNameSpace().bind(name, service);
    }

    /**
     * Unbind a service with a given name from the namespace. If the name does
     * not exist in this namespace, this method returns without an error.
     *
     * @param name
     */
    public static void unbind(Class<?> name) {
        getNameSpace().unbind(name);
    }

    /**
     * Lookup a service with a given name.
     *
     * @param name
     * @throws NameNotFoundException if the name was not found in this namespace
     */
    public static <T> T lookup(Class<T> name) throws NameNotFoundException {
        return getNameSpace().lookup(name);
    }

    /**
     * Gets a set containing all names (Class) of the bound services.
     */
    public static Set<Class<?>> nameSet() {
        return getNameSpace().nameSet();
    }

    /**
     * Get the actual {@link NameSpace} and use a default one 
     * if none was yet defined.
     * @return
     */
    private static NameSpace getNameSpace() {
        if (namespace == null) {
            namespace = new DefaultNameSpace();
        }
        return namespace;
    }
}
