/*
 *  java.lang.SecurityException
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

public class SecurityException extends RuntimeException
{
	public SecurityException() 
	{
		super();
	}

	/**
	 * Constructor for SecurityException.
	 * @param message
	 * @param cause
	 */
	public SecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for SecurityException.
	 * @param cause
	 */
	public SecurityException(Throwable cause) {
		super(cause);
	}

	public SecurityException(String s) 
	{
		super(s);
	}
}

