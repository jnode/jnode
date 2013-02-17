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
 
package org.jnode.naming;

import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;


public interface NameSpace {
    /**
     * Add a {@link NameSpaceListener} to the NameSpace
     * @param <T>
     * @param name
     * @param l
     */
    public <T> void addNameSpaceListener(Class<T> name, NameSpaceListener<T> l);
    
    /**
     * Remove a {@link NameSpaceListener} from the NameSpace
     * @param <T>
     * @param name
     * @param l
     */
    public <T> void removeNameSpaceListener(Class<T> name, NameSpaceListener<T> l);
    
    /**
     * Bind a given service in the namespace under a given name.
     *
     * @param name
     * @param service
     * @throws NameAlreadyBoundException if the name already exists within this namespace
     */
    public <T> void bind(Class<T> name, T service)
        throws NamingException, NameAlreadyBoundException;

    /**
     * Unbind a service with a given name from the namespace.
     * If the name does not exist in this namespace, this method
     * returns without an error.
     *
     * @param name
     */
    public void unbind(Class<?> name);

    /**
     * Lookup a service with a given name.
     *
     * @param name
     * @throws NameNotFoundException if the name was not found in this namespace
     */
    public <T> T lookup(Class<T> name) throws NameNotFoundException;

    /**
     * Gets a set containing all names (Class) of the bound services.
     */
    public Set<Class<?>> nameSet();
}
