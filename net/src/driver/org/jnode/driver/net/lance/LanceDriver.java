/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.BasicEthernetDriver;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class LanceDriver extends BasicEthernetDriver {
	
	public LanceDriver(LanceFlags flags) {
		this.flags = flags;
	}
	
	/**
	 * Create a new LanceCore instance
	 */
	protected AbstractDeviceCore newCore(Device device, Flags flags)
			throws DriverException, ResourceNotFreeException {
		return new LanceCore(this, device, (PCIDevice) device, flags);
	}
}
