/*
 * $Id$
 */
package org.jnode.driver.console;

/**
 * @author epr
 */
public class ConsoleException extends Exception {

	/**
	 * 
	 */
	public ConsoleException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConsoleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ConsoleException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public ConsoleException(String s) {
		super(s);
	}
}
