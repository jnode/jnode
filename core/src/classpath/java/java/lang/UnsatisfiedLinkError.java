/*
 *  java.lang.UnsatisfiedLinkError
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

public class UnsatisfiedLinkError extends LinkageError
{
	public UnsatisfiedLinkError() 
	{
		super();
	}

	/**
	 * Constructor for UnsatisfiedLinkError.
	 * @param message
	 * @param cause
	 */
	public UnsatisfiedLinkError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for UnsatisfiedLinkError.
	 * @param cause
	 */
	public UnsatisfiedLinkError(Throwable cause) {
		super(cause);
	}

	public UnsatisfiedLinkError(String s) 
	{ 
		super(s); 
	}
}

