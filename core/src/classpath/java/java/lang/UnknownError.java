/*
 *  java.lang.UnknownError
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

public class UnknownError extends VirtualMachineError
{
	public UnknownError() 
	{
		super();
	}

	/**
	 * Constructor for UnknownError.
	 * @param message
	 * @param cause
	 */
	public UnknownError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for UnknownError.
	 * @param cause
	 */
	public UnknownError(Throwable cause) {
		super(cause);
	}

	public UnknownError(String s) 
	{ 
		super(s); 
	}
}

