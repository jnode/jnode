/*
 *  java.lang.InternalError
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

public class InternalError extends VirtualMachineError
{
	public InternalError() 
	{
		super();
	}

	/**
	 * Constructor for InternalError.
	 * @param message
	 * @param cause
	 */
	public InternalError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for InternalError.
	 * @param cause
	 */
	public InternalError(Throwable cause) {
		super(cause);
	}

	public InternalError(String s) 
	{ 
		super(s); 
	}
}

