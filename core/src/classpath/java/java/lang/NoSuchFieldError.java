/*
 *  java.lang.NoSuchFieldError
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

public class NoSuchFieldError
extends IncompatibleClassChangeError
{
	public NoSuchFieldError()
	{
		super();
	}

	/**
	 * Constructor for NoSuchFieldError.
	 * @param message
	 * @param cause
	 */
	public NoSuchFieldError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NoSuchFieldError.
	 * @param cause
	 */
	public NoSuchFieldError(Throwable cause) {
		super(cause);
	}

	public NoSuchFieldError(String s)
	{
		super(s);
	}
}

