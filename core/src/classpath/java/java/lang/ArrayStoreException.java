/*
 *  java.lang.ArrayStoreException
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

public class ArrayStoreException extends RuntimeException
{
	public ArrayStoreException() 
	{ 
		super(); 
	}

	/**
	 * Constructor for ArrayStoreException.
	 * @param message
	 * @param cause
	 */
	public ArrayStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ArrayStoreException.
	 * @param cause
	 */
	public ArrayStoreException(Throwable cause) {
		super(cause);
	}

	public ArrayStoreException(String s) 
	{ 
		super(s); 
	}
}

