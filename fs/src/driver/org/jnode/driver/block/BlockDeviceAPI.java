/**
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.driver.DeviceAPI;

/**
 * <description>
 * 
 * @author epr
 */
public interface BlockDeviceAPI extends DeviceAPI {
	
	/**
	 * Gets the total length in bytes 
	 * @return long
	 * @throws IOException
	 */
	public abstract long getLength()
	throws IOException;	
	
	/**
	 * Read a block of data
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @throws IOException
	 */
	public abstract void read(long devOffset, byte[] dest, int destOffset, int length)
	throws IOException;
	
	/**
	 * Write a block of data
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @throws IOException
	 */
	public abstract void write(long devOffset, byte[] src, int srcOffset, int length)
	throws IOException;

	public abstract void flush() 
	throws IOException;
}
