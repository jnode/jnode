/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;

import org.jnode.driver.Device;

/**
 * @author epr
 */
public interface FileSystem {

	/**
	 * Gets the device this FS driver operates on. 
	 */
	public Device getDevice();
	
	/**
	 * Gets the root entry of this filesystem. This is usually a director,
	 * but this is not required.
	 */
	public FSEntry getRootEntry()
	throws IOException;
	
	/**
	 * Close this filesystem. After a close, all invocations of method of
	 * this filesystem or objects created by this filesystem will throw 
	 * an IOException.
	 * @throws IOException
	 */
	public void close()
	throws IOException;
}
