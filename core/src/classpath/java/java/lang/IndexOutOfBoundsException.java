/*
 *  java.lang.IndexOutOfBoundsException
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

public class IndexOutOfBoundsException extends RuntimeException
{
	public IndexOutOfBoundsException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for IndexOutOfBoundsException.
	 * @param message
	 * @param cause
	 */
	public IndexOutOfBoundsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IndexOutOfBoundsException.
	 * @param cause
	 */
	public IndexOutOfBoundsException(Throwable cause) {
		super(cause);
	}

	public IndexOutOfBoundsException(String s) 
	{ 
		super(s); 
	}
}

