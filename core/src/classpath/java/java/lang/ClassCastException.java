/*
 *  java.lang.ClassCastException
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

public class ClassCastException extends RuntimeException
{
	public ClassCastException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for ClassCastException.
	 * @param message
	 * @param cause
	 */
	public ClassCastException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ClassCastException.
	 * @param cause
	 */
	public ClassCastException(Throwable cause) {
		super(cause);
	}

	public ClassCastException(String s) 
	{ 
		super(s); 
	}
}

