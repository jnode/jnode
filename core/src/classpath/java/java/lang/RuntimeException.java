/*
 *  java.lang.RuntimeException
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

public class RuntimeException extends Exception
{
	public RuntimeException() 
	{
		super();
	}

	/**
	 * Constructor for RuntimeException.
	 * @param message
	 * @param cause
	 */
	public RuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for RuntimeException.
	 * @param cause
	 */
	public RuntimeException(Throwable cause) {
		super(cause);
	}

	public RuntimeException(String s) 
	{
		super(s);
	}
}

