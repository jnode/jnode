/**
 * 
 */
package org.jnode.apps.jpartition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.naming.NameSpace;

public final class BasicNameSpace implements NameSpace {
	protected final Map<Class<?>, Object> namespace = new HashMap<Class<?>, Object>();

	public <T> void bind(Class<T> name, T service)
			throws NamingException, NameAlreadyBoundException {
		namespace.put(name, service);
	}

	public <T> T lookup(Class<T> name) throws NameNotFoundException {
		return (T) namespace.get(name);
	}

	public Set<Class<?>> nameSet() {
		return namespace.keySet();
	}

	public void unbind(Class<?> name) {
		namespace.remove(name);
	}
}