/*
 *  java.lang.ClassCircularityError
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

public class ClassCircularityError extends LinkageError
{
	public ClassCircularityError() 
	{
		super();
	}

	/**
	 * Constructor for ClassCircularityError.
	 * @param message
	 * @param cause
	 */
	public ClassCircularityError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ClassCircularityError.
	 * @param cause
	 */
	public ClassCircularityError(Throwable cause) {
		super(cause);
	}

	public ClassCircularityError(String s) 
	{ 
		super(s); 
	}
}

