/*
 *  java.lang.NegativeArraySizeException
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

public class NegativeArraySizeException extends RuntimeException
{
	public NegativeArraySizeException() 
	{
		super();
	}

	/**
	 * Constructor for NegativeArraySizeException.
	 * @param message
	 * @param cause
	 */
	public NegativeArraySizeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NegativeArraySizeException.
	 * @param cause
	 */
	public NegativeArraySizeException(Throwable cause) {
		super(cause);
	}

	public NegativeArraySizeException(String s) 
	{
		super(s);
	}
}

