/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Generic exception of drivers in the device framework.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DriverException extends Exception {

	/**
	 * 
	 */
	public DriverException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DriverException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DriverException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DriverException(String s) {
		super(s);
	}
}
