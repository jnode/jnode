/*
 *  java.lang.IllegalMonitorStateException
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

public class IllegalMonitorStateException extends RuntimeException
{
	public IllegalMonitorStateException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for IllegalMonitorStateException.
	 * @param message
	 * @param cause
	 */
	public IllegalMonitorStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for IllegalMonitorStateException.
	 * @param cause
	 */
	public IllegalMonitorStateException(Throwable cause) {
		super(cause);
	}

	public IllegalMonitorStateException(String s) 
	{ 
		super(s); 
	}
}

