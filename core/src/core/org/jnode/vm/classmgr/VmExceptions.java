/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * This class holds the exceptions a method has declared to throw.
 * It is read from the Exceptions attribute of a method.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmExceptions extends VmSystemObject {
	
	private final VmConstClass[] exceptions;
	
	/**
	 * Create a new, empty instance
	 */
	public VmExceptions() {
		this.exceptions = null;
	}
	
	/**
	 * Create a new instance
	 * @param exceptions
	 */
	public VmExceptions(VmConstClass[] exceptions) {
		this.exceptions = exceptions;
	}
	
	/**
	 * Gets the number of exceptions in this list.
	 * @return int
	 */
	public final int getLength() {
		if (exceptions != null) {
			return exceptions.length;
		} else {
			return 0;
		}
	}
	
	/**
	 * Gets the exception at the given index
	 * @param index
	 * @return Exception class reference
	 */
	public final VmConstClass getException(int index) {
		if (exceptions != null) {
			return exceptions[index];
		} else {
			throw new IndexOutOfBoundsException("exceptions is empty; index " + index);
		}
	}
	
	/**
	 * Does this list contain a class with the given name?
	 * @param className
	 * @return boolean
	 */
	public final boolean contains(String className) {
		if (exceptions != null) {
			final int length = exceptions.length;
			for (int i = 0; i < length; i++) {
				final String name = exceptions[i].getClassName();
				if (name.equals(className)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Does this list contain a class with the name of the given class?
	 * @param cls
	 * @return boolean
	 */
	public final boolean contains(Class cls) {
		return contains(cls.getName());
	}
}
