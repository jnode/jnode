/*
 *  java.lang.LinkageError
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

public class LinkageError extends Error
{
	public LinkageError() 
	{
		super();
	}

	/**
	 * Constructor for LinkageError.
	 * @param message
	 * @param cause
	 */
	public LinkageError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for LinkageError.
	 * @param cause
	 */
	public LinkageError(Throwable cause) {
		super(cause);
	}

	public LinkageError(String s) 
	{ 
		super(s); 
	}
}

