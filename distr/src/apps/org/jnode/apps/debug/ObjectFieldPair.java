/*
 * $Id$
 */
package org.jnode.apps.debug;

import java.lang.reflect.Field;


/**
 * Just a bean to store a Field and an object which has that field
 * @author blind
 */
class ObjectFieldPair{
	Object object;
	Field field;
	public ObjectFieldPair(Object object, Field field) {
		this.object = object;
		this. field = field;
	}
	
	public Field getField() {
		return field;
	}

	public Object getObject() {
		return object;
	}
}