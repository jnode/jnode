/*
 * $Id$
 */
package org.jnode.driver.net._3c90x;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.AbstractEthernetDriver;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class _3c90xDriver extends AbstractEthernetDriver {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** The actual device driver */
	private _3c90xCore dd;
	/** The device flags */
	private final _3c90xFlags flags;

	/**
	 * Create a new instance
	 * 
	 * @param flags
	 */
	public _3c90xDriver(_3c90xFlags flags) {
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
	protected void doTransmitEthernet(SocketBuffer skbuf) throws NetworkException {
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
	 * Create a new _3c90xCore instance
	 */
	protected _3c90xCore newCore(Device device, _3c90xFlags flags) throws DriverException, ResourceNotFreeException {
		return new _3c90xCore(this, device, (PCIDevice) device, flags);
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		log.debug("stopDevice");
		super.stopDevice();
		dd.disable();
		dd.release();
		dd = null;
		log.debug("done");
	}

	/**
	 * Gets the device flags
	 */
	public _3c90xFlags getFlags() {
		return flags;
	}
}
