/*
 *  java.lang.IncompatibleClassChangeError
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

public class IncompatibleClassChangeError extends LinkageError
{
	public IncompatibleClassChangeError() 
	{
		super();
	}

	/**
	 * Constructor for IncompatibleClassChangeError.
	 * @param message
	 * @param cause
	 */
	public IncompatibleClassChangeError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IncompatibleClassChangeError.
	 * @param cause
	 */
	public IncompatibleClassChangeError(Throwable cause) {
		super(cause);
	}

	public IncompatibleClassChangeError(String s) 
	{ 
		super(s); 
	}
}

