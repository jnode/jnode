/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;

/**
 * @author Fabien DUMINY
 * 
 * New exception allowing to handle cases where a FileSystem is mounted readOnly
 */
public class ReadOnlyFileSystemException extends IOException {
	/**
	 * @param message
	 * @param cause
	 */
	public ReadOnlyFileSystemException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}
	/**
	 * @param cause
	 */
	public ReadOnlyFileSystemException(Throwable cause) {
		super();
		initCause(cause);
	}
	/**
	 * @param message
	 */
	public ReadOnlyFileSystemException(String message) {
		super(message);
	}
}
