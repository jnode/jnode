/*
 * $Id$
 */
package org.jnode.system;

/**
 * Requested resource is not available exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ResourceNotFreeException extends Exception {

	/**
	 * 
	 */
	public ResourceNotFreeException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ResourceNotFreeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ResourceNotFreeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public ResourceNotFreeException(String s) {
		super(s);
	}
}
