/*
 * $Id$
 */
package org.jnode.system;

/**
 * Direct Memory Access Exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DMAException extends Exception {

	/**
	 * 
	 */
	public DMAException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DMAException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DMAException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DMAException(String s) {
		super(s);
	}
}
