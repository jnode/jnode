/*
 *  java.lang.InterruptedException
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

public class InterruptedException extends Exception
{
	public InterruptedException() 
	{
		super();
	}

	/**
	 * Constructor for InterruptedException.
	 * @param message
	 * @param cause
	 */
	public InterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for InterruptedException.
	 * @param cause
	 */
	public InterruptedException(Throwable cause) {
		super(cause);
	}

	public InterruptedException(String s) 
	{
		super(s);
	}
}

