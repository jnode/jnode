/*
 *  java.lang.ClassNotFoundException
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

public class ClassNotFoundException extends Exception
{
	public ClassNotFoundException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for ClassNotFoundException.
	 * @param message
	 * @param cause
	 */
	public ClassNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ClassNotFoundException.
	 * @param cause
	 */
	public ClassNotFoundException(Throwable cause) {
		super(cause);
	}

	public ClassNotFoundException(String s) 
	{ 
		super(s); 
	}
}

