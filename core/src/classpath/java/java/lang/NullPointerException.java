/*
 *  java.lang.NullPointerException
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

public class NullPointerException extends RuntimeException
{
	public NullPointerException() 
	{
		super();
	}

	/**
	 * Constructor for NullPointerException.
	 * @param message
	 * @param cause
	 */
	public NullPointerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NullPointerException.
	 * @param cause
	 */
	public NullPointerException(Throwable cause) {
		super(cause);
	}

	public NullPointerException(String s) 
	{
		super(s);
	}
}

