/*
 *  java.lang.NumberFormatException
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

public class NumberFormatException extends IllegalArgumentException
{
	public NumberFormatException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for NumberFormatException.
	 * @param message
	 * @param cause
	 */
	public NumberFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NumberFormatException.
	 * @param cause
	 */
	public NumberFormatException(Throwable cause) {
		super(cause);
	}

	public NumberFormatException(String s) 
	{ 
		super(s); 
	}
}

