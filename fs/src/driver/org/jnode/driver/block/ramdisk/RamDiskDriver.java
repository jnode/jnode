/*
 * $Id$
 */
package org.jnode.driver.block.ramdisk;

import java.io.IOException;

import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskDriver extends Driver implements FSBlockDeviceAPI {

	/** The device */
	private RamDiskDevice device;
	/** The data */
	private byte[] data;

	/**
	 * Start the device
	 * @throws DriverException
	 */
	protected void startDevice() throws DriverException {
		try {
			this.device = (RamDiskDevice) getDevice();
			this.device.getManager().rename(device, "ramdisk", true);
			if (this.data == null) {
				this.data = new byte[device.getSize()];
			}
			this.device.registerAPI(FSBlockDeviceAPI.class, this);
		} catch (DeviceAlreadyRegisteredException ex) {
			throw new DriverException(ex);
		}
	}

	/**
	 * Stop the device
	 * @throws DriverException
	 */
	protected void stopDevice() throws DriverException {
		this.device.unregisterAPI(FSBlockDeviceAPI.class);
		//this.data = null;
		this.device = null;
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
	 * @return The partition table entry
	 */
	public PartitionTableEntry getPartitionTableEntry() {
		return null;
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
	 * @return The sector size
	 */
	public int getSectorSize() {
		return 512;
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 * @throws IOException
	 */
	public void flush() throws IOException {
		// Do nothing
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 * @throws IOException
	 */
	public long getLength() throws IOException {
		return data.length;
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
		System.arraycopy(this.data, (int) devOffset, dest, destOffset, length);
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
        System.arraycopy(src, srcOffset, this.data, (int) devOffset, length);
	}
}
