/*
 * $Id$
 */
package org.jnode.fs;

/**
 * @author epr
 */
public class FileSystemException extends Exception {

	/**
	 * 
	 */
	public FileSystemException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileSystemException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FileSystemException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public FileSystemException(String s) {
		super(s);
	}
}
