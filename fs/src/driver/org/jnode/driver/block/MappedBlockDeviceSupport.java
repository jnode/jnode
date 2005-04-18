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

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;

/**
 * @author epr
 */
public class MappedBlockDeviceSupport extends Device implements BlockDeviceAPI {

	private final Device parent;
	private final BlockDeviceAPI parentApi;
	private final long offset;
	private final long length;

	public MappedBlockDeviceSupport(Device parent, long offset, long length) throws IOException {
		super(parent.getBus(), "mapped-" + parent.getId());
		this.parent = parent;
		try {
			this.parentApi = (BlockDeviceAPI)parent.getAPI(BlockDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			final IOException ioe = new IOException("BlockDeviceAPI not found on device");
			ioe.initCause(ex);
			throw ioe;
		}
		this.offset = offset;
		this.length = length;
		if (offset < 0) {
			throw new IndexOutOfBoundsException("offset < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (offset + length > parentApi.getLength()) {
			throw new IndexOutOfBoundsException(
                        "offset("+offset+") + length("+length+
                        ") > parent.length("+parentApi.getLength()+")");
		}
		registerAPI(BlockDeviceAPI.class, this);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 * @throws IOException
	 */
	public void read(long devOffset, byte[] dest, int destOffset, int length) throws IOException {
		if (devOffset < 0) {
			throw new IOException("Out of mapping: offset < 0");
		}
		if (length < 0) {
			throw new IOException("Out of mapping: length < 0");
		}
		if (devOffset + length > this.length) {
			throw new IOException("Out of mapping: devOffset(" + devOffset + ") + length(" + length + ") > this.length(" + this.length + ")");
		}
		parentApi.read(offset + devOffset, dest, destOffset, length);
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 * @throws IOException
	 */
	public void write(long devOffset, byte[] src, int srcOffset, int length) throws IOException {
		if (devOffset < 0) {
			throw new IOException("Out of mapping: offset < 0");
		}
		if (length < 0) {
			throw new IOException("Out of mapping: length < 0");
		}
		if (devOffset + length > this.length) {
			throw new IOException("Out of mapping: devOffset(" + devOffset + ") + length(" + length + ") > this.length(" + this.length + ")");
		}
		parentApi.write(offset + devOffset, src, srcOffset, length);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 * @throws IOException
	 */
	public void flush() throws IOException {
		parentApi.flush();
	}

	/**
	 * @return long
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return Device
	 */
	public Device getParent() {
		return parent;
	}
}
