/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NotResolvedYetException extends RuntimeException {

	/**
	 * 
	 */
	public NotResolvedYetException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotResolvedYetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NotResolvedYetException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public NotResolvedYetException(String s) {
		super(s);
	}

}
