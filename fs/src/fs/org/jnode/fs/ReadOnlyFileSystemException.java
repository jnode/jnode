/*
 * Created on 30 mars 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
		super(message, cause);
	}
	/**
	 * @param cause
	 */
	public ReadOnlyFileSystemException(Throwable cause) {
		super(cause);
	}
	/**
	 * @param message
	 */
	public ReadOnlyFileSystemException(String message) {
		super(message);
	}
}
