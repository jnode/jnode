/*
 * $Id$
 */
package org.jnode.apps.debug;

import java.lang.reflect.Method;

/**
 * Just a bean to store a Method and an object which has that method
 * @author blind
 */
class ObjectMethodPair {
	Object object;
	Method method;
	public ObjectMethodPair(Object object, Method method) {
		this.object = object;
		this. method = method;
	}
	
	public Method getMethod() {
		return method;
	}

	public Object getObject() {
		return object;
	}
}

