/*
 *  java.lang.IllegalArgumentException
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

public class IllegalArgumentException extends RuntimeException
{
	public IllegalArgumentException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for IllegalArgumentException.
	 * @param message
	 * @param cause
	 */
	public IllegalArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IllegalArgumentException.
	 * @param cause
	 */
	public IllegalArgumentException(Throwable cause) {
		super(cause);
	}

	public IllegalArgumentException(String s) 
	{ 
		super(s); 
	}
}

