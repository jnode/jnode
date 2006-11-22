/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.block;

import java.io.IOException;
import java.nio.ByteBuffer;

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
	 * @throws IOException
	 */
	public abstract void read(long devOffset, ByteBuffer dest) throws IOException;
	
	/**
	 * Write a block of data
	 * @param devOffset
	 * @param src
	 * @throws IOException
	 */
	public abstract void write(long devOffset, ByteBuffer src) throws IOException;

	/**
	 * flush data in caches to the block device
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;
}
