/*
 *  java.lang.AbstractMethodError
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

public class AbstractMethodError extends IncompatibleClassChangeError
{
	public AbstractMethodError() 
	{
		super();
	}

	/**
	 * Constructor for AbstractMethodError.
	 * @param message
	 * @param cause
	 */
	public AbstractMethodError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for AbstractMethodError.
	 * @param cause
	 */
	public AbstractMethodError(Throwable cause) {
		super(cause);
	}

	public AbstractMethodError(String s) 
	{ 
		super(s); 
	}
}

