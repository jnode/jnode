/*
 *  java.lang.StackOverflowError
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

public class StackOverflowError extends VirtualMachineError {
	public StackOverflowError() {
		super();
	}

	/**
	 * Constructor for StackOverflowError.
	 * @param message
	 * @param cause
	 */
	public StackOverflowError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for StackOverflowError.
	 * @param cause
	 */
	public StackOverflowError(Throwable cause) {
		super(cause);
	}

	public StackOverflowError(String s) {
		super(s);
	}
}
