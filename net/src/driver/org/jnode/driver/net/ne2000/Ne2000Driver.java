/*
 * $Id$
 */
package org.jnode.driver.net.ne2000;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.AbstractEthernetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public abstract class Ne2000Driver extends AbstractEthernetDriver {

	/** The actual device driver */
	private Ne2000Core dd;
	/** The device flags */
	private final Ne2000Flags flags;

	/**
	 * Create a new instance
	 * @param flags
	 */
	public Ne2000Driver(Ne2000Flags flags) {
		this.flags = flags;
	}

	/**
	 * Gets the hardware address of this device
	 */
	public HardwareAddress getAddress() {
		return dd.getHwAddress();
	}

	/**
	 * @see org.jnode.driver.net.AbstractNetDriver#doTransmit(SocketBuffer, HardwareAddress)
	 */
	protected void doTransmitEthernet(SocketBuffer skbuf)
		throws NetworkException {
		try {
			// Pad
			if (skbuf.getSize() < ETH_ZLEN) {
				skbuf.append(ETH_ZLEN - skbuf.getSize());
			}

			dd.transmit(skbuf, 5000);
		} catch (InterruptedException ex) {
			throw new NetworkException("Interrupted", ex);
		} catch (TimeoutException ex) {
			throw new NetworkException("Timeout", ex);
		}
	}

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		try {
			dd = newCore(getDevice(), flags);
			dd.initialize();
			super.startDevice();
		} catch (ResourceNotFreeException ex) {
			throw new DriverException("Cannot claim " + flags.getName() + " resources", ex);
		}
	}

	/**
	 * Create a new Ne2000Core instance
	 */
	protected abstract Ne2000Core newCore(Device device, Ne2000Flags flags)
	throws DriverException, ResourceNotFreeException;

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		super.stopDevice();
		dd.disable();
		dd.release();
		dd = null;
	}
	
	/**
	 * Gets the device flags
	 */
	public Ne2000Flags getFlags() {
		return flags;
	}
}
