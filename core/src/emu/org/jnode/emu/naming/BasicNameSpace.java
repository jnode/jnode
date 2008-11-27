/*
 * $Id: Label.java 4159 2008-05-30 16:15:41Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.emu.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.jnode.naming.AbstractNameSpace;

/**
 * This implementation of NameSpace does not make use of Class.getVmClass() and
 * therefore can be used in JNode applications / test cases / frameworks designed 
 * to run on a classic Java VM.
 * 
 * @author crawley@jnode.org
 */
public final class BasicNameSpace extends AbstractNameSpace {
    protected final Map<Class<?>, Object> namespace = new HashMap<Class<?>, Object>();

    public <T> void bind(Class<T> name, T service)
        throws NamingException, NameAlreadyBoundException {
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

    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> name) throws NameNotFoundException {
        synchronized (namespace) {
            T res = (T) namespace.get(name);
            if (res == null) {
                throw new NameNotFoundException(name.getName());
            }
            return res;
        }
    }

    public Set<Class<?>> nameSet() {
        return namespace.keySet();
    }

    public void unbind(Class<?> name) {
        namespace.remove(name);
    }
}
