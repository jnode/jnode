/*
 *  java.lang.NoClassDefFoundError
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

public class NoClassDefFoundError extends LinkageError
{
	public NoClassDefFoundError() 
	{
		super();
	}

	public NoClassDefFoundError(String s) 
	{ 
		super(s); 
	}
}

