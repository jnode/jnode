/*
 * $Id$
 */
package org.jnode.driver;

/**
 * API has not been found exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ApiNotFoundException extends DeviceException {

	/**
	 * 
	 */
	public ApiNotFoundException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ApiNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ApiNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public ApiNotFoundException(String s) {
		super(s);
	}
}
