/*
 *  java.lang.ArrayIndexOutOfBoundsException
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

public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException
{
	public ArrayIndexOutOfBoundsException() 
	{
		super();
	}

	public ArrayIndexOutOfBoundsException(String s) 
	{
		super(s);
	}

	/**
	 * Constructor for ArrayIndexOutOfBoundsException.
	 * @param message
	 * @param cause
	 */
	public ArrayIndexOutOfBoundsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for ArrayIndexOutOfBoundsException.
	 * @param cause
	 */
	public ArrayIndexOutOfBoundsException(Throwable cause) {
		super(cause);
	}

	public ArrayIndexOutOfBoundsException(int index) 
	{
		this("Array index out of bounds: " + index);
	}
}

