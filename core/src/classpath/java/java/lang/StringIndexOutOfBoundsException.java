/*
 *  java.lang.StringIndexOutOfBoundsException
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

public class StringIndexOutOfBoundsException extends IndexOutOfBoundsException
{
	public StringIndexOutOfBoundsException() 
	{ 
		super(); 
	}

	public StringIndexOutOfBoundsException(String s) 
	{ 
		super(s); 
	}

	/**
	 * Constructor for StringIndexOutOfBoundsException.
	 * @param message
	 * @param cause
	 */
	public StringIndexOutOfBoundsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for StringIndexOutOfBoundsException.
	 * @param cause
	 */
	public StringIndexOutOfBoundsException(Throwable cause) {
		super(cause);
	}

	public StringIndexOutOfBoundsException(int index) 
	{ 
		this("String index out of bounds: " + index); 
	}
}

