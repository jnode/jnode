/*
 *  java.lang.Error
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

public class Error extends Throwable {
	public Error() {
		super();
	}

	/**
	 * Constructor for Error.
	 * @param message
	 * @param cause
	 */
	public Error(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for Error.
	 * @param cause
	 */
	public Error(Throwable cause) {
		super(cause);
	}

	public Error(String s) {
		super(s);
	}
}
