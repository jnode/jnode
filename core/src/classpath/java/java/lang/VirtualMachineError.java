/*
 *  java.lang.VirtualMachineError
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

public class VirtualMachineError extends Error
{
	public VirtualMachineError() 
	{
		super();
	}

	/**
	 * Constructor for VirtualMachineError.
	 * @param message
	 * @param cause
	 */
	public VirtualMachineError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for VirtualMachineError.
	 * @param cause
	 */
	public VirtualMachineError(Throwable cause) {
		super(cause);
	}

	public VirtualMachineError(String s) 
	{ 
		super(s); 
	}
}

