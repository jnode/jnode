/**
 * $Id$
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
			throw new IOException("BlockDeviceAPI not found on parent device", ex);
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
			throw new IndexOutOfBoundsException("offset + length > parent.length");
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
			throw new IOException("Out of mapping: devOffset + length > this.length");
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
			throw new IOException("Out of mapping: devOffset + length > this.length");
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
