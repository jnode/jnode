/*
 * $Id$
 */
package org.jnode.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

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
	private static final Map namespace = new HashMap();

	/**
	 * Bind a given service in the namespace under a given name.
	 * @param name
	 * @param service
	 * @throws NameAlreadyBoundException if the name already exists within this namespace
	 */
	public static void bind(String name, Object service) 
	throws NamingException, NameAlreadyBoundException {
		synchronized (namespace) {
			if (namespace.containsKey(name)) {
				throw new NameAlreadyBoundException(name);
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
	public static void unbind(String name) {
		synchronized (namespace) {
			namespace.remove(name);
		}
	}

	/**
	 * Lookup a service with a given name.
	 * @param name
	 * @throws NameNotFoundException if the name was not found in this namespace
	 */
	public static Object lookup(String name) 
	throws NameNotFoundException {
		final Object result = namespace.get(name);
		if (result == null) {
			throw new NameNotFoundException(name);
		}
		return result;
	}

	/**
	 * Gets a set containing all names (String) of the bound services.
	 */
	public static Set nameSet() {
		final TreeSet result;
		synchronized (namespace) {
			result = new TreeSet(namespace.keySet());
		}
		return result;
	}
}
