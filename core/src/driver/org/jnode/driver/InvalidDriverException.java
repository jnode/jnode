/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Driver is not valid for a device exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvalidDriverException extends DriverException {

	/**
	 * 
	 */
	public InvalidDriverException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidDriverException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public InvalidDriverException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public InvalidDriverException(String s) {
		super(s);
	}
}
