/*
 *  java.lang.CloneNotSupportedException
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

public class CloneNotSupportedException extends Exception
{
	public CloneNotSupportedException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for CloneNotSupportedException.
	 * @param message
	 * @param cause
	 */
	public CloneNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for CloneNotSupportedException.
	 * @param cause
	 */
	public CloneNotSupportedException(Throwable cause) {
		super(cause);
	}

	public CloneNotSupportedException(String s) 
	{ 
		super(s); 
	}
}
