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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;

/**
 * Partial implementation of {@link NameSpace} interface : 
 * only listener stuff has been implemented. 
 * 
 * @author fabien
 *
 */
public abstract class AbstractNameSpace implements NameSpace {

    private final Map<Class<?>, List<NameSpaceListener<?>>> listeners = 
        new HashMap<Class<?>, List<NameSpaceListener<?>>>();
    
    public final <T> void addNameSpaceListener(Class<T> name, NameSpaceListener<T> l) {
        List<NameSpaceListener<?>> list = listeners.get(name);
        if (list == null) {
            // there was not yet any listener for that name => create a list
            list = new ArrayList<NameSpaceListener<?>>(); 
            listeners.put(name, list);
        }

        if (!list.contains(l)) {
            list.add(l);
        }
        
        try {
            // if a service is already bound for that name => 
            // notify the listener right now
            l.serviceBound(lookup(name));
        } catch (NameNotFoundException e) {
            // no service bound for that name => ignore
        }
    }
    
    public final <T> void removeNameSpaceListener(Class<T> name, NameSpaceListener<T> l) {
        List<NameSpaceListener<?>> list = listeners.get(name);
        if (list != null) {
            list.remove(l);
            
            if (list.isEmpty()) {
                // no more listeners for that name => remove the empty list
                listeners.remove(name);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> void fireServiceBound(Class<T> name, T service) {
        List<NameSpaceListener<?>> list = listeners.get(name);
        if (list != null) {
            for (NameSpaceListener<?> l : list) {
                ((NameSpaceListener<T>) l).serviceBound(service);
            }
        }        
    }
    

    @SuppressWarnings("unchecked")
    protected final <T> void fireServiceUnbound(Class<T> name, Object service) {
        List<NameSpaceListener<?>> list = listeners.get(name);
        if (list != null) {
            for (NameSpaceListener<?> l : list) {
                NameSpaceListener<T> listener = (NameSpaceListener<T>) l; 
                listener.serviceUnbound((T) service);
            }
        }        
    }    
}
