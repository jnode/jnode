/*
 *  java.lang.IllegalThreadStateException
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

public class IllegalThreadStateException extends IllegalArgumentException
{
	public IllegalThreadStateException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for IllegalThreadStateException.
	 * @param message
	 * @param cause
	 */
	public IllegalThreadStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IllegalThreadStateException.
	 * @param cause
	 */
	public IllegalThreadStateException(Throwable cause) {
		super(cause);
	}

	public IllegalThreadStateException(String s) 
	{ 
		super(s); 
	}
}

