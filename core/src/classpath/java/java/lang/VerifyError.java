/*
 *  java.lang.VerifyError
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

public class VerifyError extends LinkageError
{
	public VerifyError() 
	{
		super();
	}

	/**
	 * Constructor for VerifyError.
	 * @param message
	 * @param cause
	 */
	public VerifyError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for VerifyError.
	 * @param cause
	 */
	public VerifyError(Throwable cause) {
		super(cause);
	}

	public VerifyError(String s) 
	{ 
		super(s); 
	}
}

