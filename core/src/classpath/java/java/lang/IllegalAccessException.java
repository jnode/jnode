/*
 *  java.lang.IllegalAccessException
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

public class IllegalAccessException extends Exception
{
	public IllegalAccessException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for IllegalAccessException.
	 * @param message
	 * @param cause
	 */
	public IllegalAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IllegalAccessException.
	 * @param cause
	 */
	public IllegalAccessException(Throwable cause) {
		super(cause);
	}

	public IllegalAccessException(String s) 
	{ 
		super(s); 
	}
}

