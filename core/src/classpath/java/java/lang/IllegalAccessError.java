/*
 *  java.lang.IllegalAccessError
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

public class IllegalAccessError extends IncompatibleClassChangeError
{
	public IllegalAccessError() 
	{
		super();
	}

	/**
	 * Constructor for IllegalAccessError.
	 * @param message
	 * @param cause
	 */
	public IllegalAccessError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IllegalAccessError.
	 * @param cause
	 */
	public IllegalAccessError(Throwable cause) {
		super(cause);
	}

	public IllegalAccessError(String s) 
	{ 
		super(s); 
	}
}

