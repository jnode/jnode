/**
 * $Id$
 */
package org.jnode.driver.block;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.InvalidDriverException;

/**
 * @author epr
 */
public class ByteArrayDevice extends Device implements BlockDeviceAPI {

	private byte[] array;

	public ByteArrayDevice(byte[] array) {
		super(null, "byte-array" + System.currentTimeMillis());
		this.array = array;
		registerAPI(BlockDeviceAPI.class, this);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 */
	public long getLength() {
		return array.length;
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 */
	public void read(long devOffset, byte[] dest, int destOffset, int length) {
		System.arraycopy(array, (int)devOffset, dest, destOffset, length);
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 */
	public void write(long devOffset, byte[] src, int srcOffset, int length) {
		System.arraycopy(src, srcOffset, array, (int)devOffset, length);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 */
	public void flush() {
		/* Nothing to do here */
	}

	/**
	 * @see org.jnode.driver.Device#getDriver()
	 * @return Driver
	 */
	public Driver getDriver() {
		return null;
	}

	/**
	 * @param driver
	 * @see org.jnode.driver.Device#setDriver(org.jnode.driver.Driver)
	 * @throws InvalidDriverException
	 */
	public void setDriver(Driver driver) throws InvalidDriverException {
		throw new InvalidDriverException("No driver allowed here.");
	}
}
