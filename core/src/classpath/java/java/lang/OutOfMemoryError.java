/*
 *  java.lang.OutOfMemoryError
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

public class OutOfMemoryError extends VirtualMachineError
{
	public OutOfMemoryError() 
	{
		super();
	}

	/**
	 * Constructor for OutOfMemoryError.
	 * @param message
	 * @param cause
	 */
	public OutOfMemoryError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for OutOfMemoryError.
	 * @param cause
	 */
	public OutOfMemoryError(Throwable cause) {
		super(cause);
	}

	public OutOfMemoryError(String s) 
	{ 
		super(s); 
	}
	
	/**
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	/*public Throwable fillInStackTrace() {
		return this;
	}*/
}

