/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.AbstractEthernetDriver;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class LanceDriver extends AbstractEthernetDriver {
	
	/** The actual device driver */
	private Lance dd;
	/** Lance type flags */
	private final LanceFlags flags;
	
	public LanceDriver(LanceFlags flags) {
		this.flags = flags;
	}

	/**
	 * Gets the hardware address of this device
	 */
	public HardwareAddress getAddress() {
		return dd.getAddress();
	}

	/**
	 * @see org.jnode.driver.net.AbstractNetDriver#doTransmit(SocketBuffer, HardwareAddress)
	 */
	protected void doTransmitEthernet(SocketBuffer skbuf) 
	throws NetworkException {
		// TODO Implement me
	}

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		try {
			dd = new Lance(getDevice(), (PCIDevice)getDevice(), flags);
			dd.initialize();
			super.startDevice();
		} catch (ResourceNotFreeException ex) {
			throw new DriverException("Cannot claim Lance resources", ex);
		}
	}

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
	 * @see org.jnode.driver.Driver#verifyConnect(org.jnode.driver.Device)
	 */
	protected void verifyConnect(Device device) throws DriverException {
		super.verifyConnect(device);
		if (!(device instanceof PCIDevice)) {
			throw new DriverException("Only PCI Lance devices are supported");
		}
	}
}
