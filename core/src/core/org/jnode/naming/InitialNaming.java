/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

import org.jnode.system.BootLog;

/**
 * This class provides a namespace that is used by the JNode system.
 * Various services are bound into this namespace.
 * <p/>
 * A service bound into this namespace can be any object. Is does not have to
 * implement any particular interface. It is up to the user of this namespace
 * to cast the objects as needed.
 * <p/>
 * Only a single service can be bound under each name. There is no restriction 
 * on the syntax of a name. Nor is there any interpretation of a name by this namespace.
 * 
 * @author epr
 */
public class InitialNaming {
	
	/** All bound names+services */
	private static NameSpace namespace;

    public static void setNameSpace(NameSpace namespace)
    {
        if(InitialNaming.namespace != null)
        {
            throw new SecurityException("namespace can't be modified after first initialization");
        }
        InitialNaming.namespace = namespace;
    }
    
	/**
	 * Bind a given service in the namespace under a given name.
	 * @param name
	 * @param service
	 * @throws NameAlreadyBoundException if the name already exists within this namespace
	 */
	public static void bind(Class name, Object service) 
	throws NamingException, NameAlreadyBoundException {
        getNameSpace().bind(name, service);	
    }
	
	/**
	 * Unbind a service with a given name from the namespace.
	 * If the name does not exist in this namespace, this method
	 * returns without an error.
	 * @param name
	 */
	public static void unbind(Class name) {
        getNameSpace().unbind(name);
	}

	/**
	 * Lookup a service with a given name.
	 * @param name
	 * @throws NameNotFoundException if the name was not found in this namespace
	 */
	public static Object lookup(Class name) 
	throws NameNotFoundException {
		return getNameSpace().lookup(name);
	}

    /**
     * Lookup a service with a given name.
     * @param name
     * @param defaultImplClass
     * @throws NameNotFoundException if the name was not found in this namespace
     */
    public static Object lookup(Class name, Class defaultImplClass) 
    throws NameNotFoundException {
        if(!getNameSpace().nameSet().contains(name))
        {
            Object defaultImpl;
            try
            {
                defaultImpl = defaultImplClass.newInstance();
                bind(name, defaultImpl);
            }
            catch (InstantiationException e)
            {
                BootLog.error("can't create default impl for "+name.getName(), e);
            }
            catch (IllegalAccessException e)
            {
                BootLog.error("can't create default impl for "+name.getName(), e);
            }
            catch (NameAlreadyBoundException e)
            {
                BootLog.error("unexpected error"+name.getName(), e);
            }
            catch (NamingException e)
            {
                BootLog.error("unexpected error"+name.getName(), e);
            }
        }
        
        return getNameSpace().lookup(name);
    }
    
	/**
	 * Gets a set containing all names (Class) of the bound services.
	 */
	public static Set nameSet() {
		return getNameSpace().nameSet();
	}

    private static NameSpace getNameSpace()
    {
        if(namespace == null)
        {
            namespace = new DefaultNameSpace();
        }
        return namespace;
    }
    
    public interface NameSpace {
        /**
         * Bind a given service in the namespace under a given name.
         * @param name
         * @param service
         * @throws NameAlreadyBoundException if the name already exists within this namespace
         */
        public void bind(Class name, Object service) 
        throws NamingException, NameAlreadyBoundException;
        
        /**
         * Unbind a service with a given name from the namespace.
         * If the name does not exist in this namespace, this method
         * returns without an error.
         * @param name
         */
        public void unbind(Class name);

        /**
         * Lookup a service with a given name.
         * @param name
         * @throws NameNotFoundException if the name was not found in this namespace
         */
        public Object lookup(Class name) throws NameNotFoundException;

        /**
         * Gets a set containing all names (Class) of the bound services.
         */
        public Set nameSet();
    }
    
    static public class DefaultNameSpace implements NameSpace {
        
        /** All bound names+services */
        protected final Map namespace = new HashMap();

        /**
         * Bind a given service in the namespace under a given name.
         * @param name
         * @param service
         * @throws NameAlreadyBoundException if the name already exists within this namespace
         */
        public void bind(Class name, Object service) 
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
        
        /**
         * Unbind a service with a given name from the namespace.
         * If the name does not exist in this namespace, this method
         * returns without an error.
         * @param name
         */
        public void unbind(Class name) {
            synchronized (namespace) {
                namespace.remove(name);
            }
        }

        /**
         * Lookup a service with a given name.
         * @param name
         * @throws NameNotFoundException if the name was not found in this namespace
         */
        public Object lookup(Class name) 
        throws NameNotFoundException {
            final Object result = namespace.get(name);
            if (result == null) {
                throw new NameNotFoundException(name.getName());
            }
            return result;
        }

        /**
         * Gets a set containing all names (Class) of the bound services.
         */
        public Set nameSet() {
            final Set result;
            synchronized (namespace) {
                result = new HashSet(namespace.keySet());
            }
            return result;
        }
    }    
}
