/*
 *  java.lang.NoSuchMethodException
 *
 *  (c) 1997 George David Morrison
 *
 *  API version: 1.0.2
 *
 *  History:
 *  01JAN1997  George David Morrison
 *    Initial version
 */

package java.lang;

public class NoSuchMethodException extends Exception {
	
	public NoSuchMethodException() {
		super();
	}

	/**
	 * Constructor for NoSuchMethodException.
	 * @param message
	 * @param cause
	 */
	public NoSuchMethodException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NoSuchMethodException.
	 * @param cause
	 */
	public NoSuchMethodException(Throwable cause) {
		super(cause);
	}

	public NoSuchMethodException(String s) {
		super(s);
	}
}
