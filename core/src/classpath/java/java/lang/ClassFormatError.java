/*
 *  java.lang.ClassFormatError
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

public class ClassFormatError extends LinkageError
{
	public ClassFormatError() 
	{
		super();
	}

	/**
	 * Constructor for ClassFormatError.
	 * @param message
	 * @param cause
	 */
	public ClassFormatError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ClassFormatError.
	 * @param cause
	 */
	public ClassFormatError(Throwable cause) {
		super(cause);
	}

	public ClassFormatError(String s) 
	{ 
		super(s); 
	}
}

