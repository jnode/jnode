/*
 *  java.lang.InstantiationError
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

public class InstantiationError extends IncompatibleClassChangeError
{
	public InstantiationError() 
	{
		super();
	}

	/**
	 * Constructor for InstantiationError.
	 * @param message
	 * @param cause
	 */
	public InstantiationError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for InstantiationError.
	 * @param cause
	 */
	public InstantiationError(Throwable cause) {
		super(cause);
	}

	public InstantiationError(String s) 
	{ 
		super(s); 
	}
}

