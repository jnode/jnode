/*
 *  java.lang.Process
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

import java.io.InputStream;
import java.io.OutputStream;

public abstract class Process 
{
	public abstract OutputStream getOutputStream();
	public abstract InputStream getInputStream();
	public abstract InputStream getErrorStream();
	public abstract int waitFor() throws InterruptedException;
	public abstract int exitValue() throws IllegalThreadStateException;
	public abstract void destroy();
	
	protected abstract void exit(int status);
}

