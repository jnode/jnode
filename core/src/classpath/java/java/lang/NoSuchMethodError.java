/*
 *  java.lang.NoSuchMethodError
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

public class NoSuchMethodError extends IncompatibleClassChangeError
{
	public NoSuchMethodError() 
	{
		super();
	}

	/**
	 * Constructor for NoSuchMethodError.
	 * @param message
	 * @param cause
	 */
	public NoSuchMethodError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NoSuchMethodError.
	 * @param cause
	 */
	public NoSuchMethodError(Throwable cause) {
		super(cause);
	}

	public NoSuchMethodError(String s) 
	{ 
		super(s); 
	}
}

