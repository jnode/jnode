/*
 * $Id$
 */
package org.jnode.driver.block.ramdisk;

import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskDriver extends Driver implements FSBlockDeviceAPI {

	private static final String RAMDISK_DEFAULTNAME = "ramdisk";
	/** The device */
	private RamDiskDevice device;
	/** The data */
	private byte[] data;
	private String devName;

	/**
	 * Create a RamDisk Driver
	 * 
	 * @param deviceName
	 *           null will name it ramdisk with autonumber
	 */
	public RamDiskDriver(String deviceName) {
		this.devName = deviceName;
	}
	/**
	 * Start the device
	 * 
	 * @throws DriverException
	 */
	protected void startDevice() throws DriverException {
		try {
			this.device = (RamDiskDevice)getDevice();
			if (this.devName == null) {
				this.device.getManager().rename(device, RAMDISK_DEFAULTNAME, true);
			} else {
				this.device.getManager().rename(device, devName, false);
			}

			if (this.data == null) {
				this.data = new byte[device.getSize()];
				this.device.registerAPI(BlockDeviceAPI.class, this);
			} else {
				this.device.registerAPI(FSBlockDeviceAPI.class, this);
			}
		} catch (DeviceAlreadyRegisteredException ex) {
			throw new DriverException(ex);
		}
	}

	/**
	 * Stop the device
	 */
	protected void stopDevice() {
		this.device.unregisterAPI(FSBlockDeviceAPI.class);
		this.device.unregisterAPI(BlockDeviceAPI.class);
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
	 */
	public void flush() {
		// Do nothing
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 */
	public long getLength() {
		return data.length;
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 */
	public void read(long devOffset, byte[] dest, int destOffset, int length) {
		System.arraycopy(this.data, (int)devOffset, dest, destOffset, length);
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 */
	public void write(long devOffset, byte[] src, int srcOffset, int length) {
		System.arraycopy(src, srcOffset, this.data, (int)devOffset, length);
	}
}
