/*
 * $Id$
 */

package org.jnode.driver.net.ethernet.spi;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author Martin Husted Hartvig
 */

public abstract class BasicEthernetDriver extends AbstractEthernetDriver {
    
	/** The device flags */
	protected Flags flags;
	/** The actual device driver */
	private AbstractDeviceCore abstractDeviceCore;
	/** My logger */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(SocketBuffer, HardwareAddress)
	 */
	protected void doTransmitEthernet(SocketBuffer skbuf) throws NetworkException {
		try {
			// Pad
			if (skbuf.getSize() < ETH_ZLEN) {
				skbuf.append(ETH_ZLEN - skbuf.getSize());
			}

			abstractDeviceCore.transmit(skbuf, 5000);
		} catch (InterruptedException ex) {
			throw new NetworkException("Interrupted", ex);
		} catch (TimeoutException ex) {
			throw new NetworkException("Timeout", ex);
		}
	}

	/**
	 * Gets the hardware address of this device
	 */
	public HardwareAddress getAddress() {
		return abstractDeviceCore.getHwAddress();
	}

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		try {
		    log.info("Starting " + flags.getName());
			abstractDeviceCore = newCore(getDevice(), flags);
			abstractDeviceCore.initialize();

			super.startDevice();
		} catch (ResourceNotFreeException ex) {
			throw new DriverException("Cannot claim " + flags.getName() + " resources", ex);
		}
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		super.stopDevice();

		abstractDeviceCore.disable();
		abstractDeviceCore.release();
		abstractDeviceCore = null;
	}

	/**
	 * Create a new RTL8139Core instance
	 */
	protected abstract AbstractDeviceCore newCore(Device device, Flags flags) throws DriverException, ResourceNotFreeException;

	/**
	 *
	 * Get the flags for this device
	 *
	 * @return The flags
	 */
	public Flags getFlags() {
		return flags;
	}
}
