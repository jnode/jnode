/*
 * $Id$
 */
package org.jnode.driver.net.ne2000.pci;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ne2000.Ne2000Core;
import org.jnode.driver.net.ne2000.Ne2000Driver;
import org.jnode.driver.net.ne2000.Ne2000Flags;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class Ne2000PCIDriver extends Ne2000Driver {
	
	/**
	 * Create a new instance
	 * @param flags
	 */
	public Ne2000PCIDriver(Ne2000Flags flags) {
		super(flags);
	}

	/**
	 * Create a new Ne2000Core instance
	 * @see org.jnode.driver.net.ne2000.Ne2000Driver#newCore(org.jnode.driver.Device, org.jnode.driver.net.ne2000.Ne2000Flags)
	 */
	protected Ne2000Core newCore(Device device, Ne2000Flags flags)
	throws DriverException, ResourceNotFreeException {
		return new Ne2000PCI(this, device, (PCIDevice)device, flags);
	}

	/**
	 * @see org.jnode.driver.Driver#verifyConnect(org.jnode.driver.Device)
	 */
	protected void verifyConnect(Device device) throws DriverException {
		super.verifyConnect(device);
		if (!(device instanceof PCIDevice)) {
			throw new DriverException("Only NE2000-PCI devices are supported");
		}
	}
}
