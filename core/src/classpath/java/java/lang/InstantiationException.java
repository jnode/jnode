/*
 *  java.lang.InstantiationException
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

public class InstantiationException extends Exception
{
	public InstantiationException() 
	{
		super();
	}

	/**
	 * Constructor for InstantiationException.
	 * @param message
	 * @param cause
	 */
	public InstantiationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for InstantiationException.
	 * @param cause
	 */
	public InstantiationException(Throwable cause) {
		super(cause);
	}

	public InstantiationException(String s) 
	{
		super(s);
	}
}

