/*
 * $Id$
 */
package org.jnode.vm;

/**
 * @author epr
 */
public abstract class ObjectVisitor extends VmSystemObject  {

	/**
	 * Generic visit method for objects.
	 * @param object
	 * @return true to continue with the next object, false to stop calling this method.
	 */
	public abstract boolean visit(Object object);

}
