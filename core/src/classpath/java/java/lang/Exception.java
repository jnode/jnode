/*
 *  java.lang.Exception
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

public class Exception extends Throwable {
	
	public Exception() {
		super();
	}

	/**
	 * Constructor for Exception.
	 * @param message
	 * @param cause
	 */
	public Exception(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for Exception.
	 * @param cause
	 */
	public Exception(Throwable cause) {
		super(cause);
	}

	public Exception(String s) {
		super(s);
	}
}
