/*
 * $Id$
 */
package org.jnode.shell;

/**
 * @author epr
 */
public class ShellException extends Exception {

	/**
	 * 
	 */
	public ShellException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ShellException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ShellException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public ShellException(String s) {
		super(s);
	}
}
