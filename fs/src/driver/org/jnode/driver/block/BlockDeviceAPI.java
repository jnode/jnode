/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
