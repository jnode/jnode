/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class TimeoutException extends Exception {

	/**
	 * 
	 */
	public TimeoutException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public TimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public TimeoutException(String s) {
		super(s);
	}
}
