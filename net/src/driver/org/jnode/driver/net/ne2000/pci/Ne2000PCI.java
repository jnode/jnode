/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.net.ne2000.pci;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ne2000.Ne2000Core;
import org.jnode.driver.net.ne2000.Ne2000Flags;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;

/**
 * @author epr
 */
public class Ne2000PCI extends Ne2000Core {
	
	/**
	 * Create a new instance
	 * @param owner
	 * @param device
	 * @param flags
	 */
	public Ne2000PCI(Ne2000PCIDriver driver, ResourceOwner owner, PCIDevice device, Ne2000Flags flags) 
	throws ResourceNotFreeException, DriverException {
		super(driver, owner, device, flags);
	}
	/**
	 * Gets the first IO-Address used by the given device
	 * @param device
	 * @param flags
	 */
	protected int getIOBase(Device device, Ne2000Flags flags) 
	throws DriverException {
		final PCIDeviceConfig config = ((PCIDevice)device).getConfig();
		final PCIBaseAddress[] addrs = config.getBaseAddresses();
		if (addrs.length < 1) {
			throw new DriverException("Cannot find iobase: not base addresses");
		}
		if (!addrs[0].isIOSpace()) {
			throw new DriverException("Cannot find iobase: first address is not I/O");
		}
		return addrs[0].getIOBase();
	}

	/**
	 * Gets the number of IO-Addresses used by the given device
	 * @param device
	 * @param flags
	 */
	protected int getIOLength(Device device, Ne2000Flags flags)
	throws DriverException {
		final PCIDeviceConfig config = ((PCIDevice)device).getConfig();
		final PCIBaseAddress[] addrs = config.getBaseAddresses();
		if (addrs.length < 1) {
			throw new DriverException("Cannot find iobase: not base addresses");
		}
		if (!addrs[0].isIOSpace()) {
			throw new DriverException("Cannot find iobase: first address is not I/O");
		}
		return addrs[0].getSize();
	}

	/**
	 * Gets the IRQ used by the given device
	 * @param device
	 * @param flags
	 */
	protected int getIRQ(Device device, Ne2000Flags flags) 
	throws DriverException {
		final PCIDeviceConfig config = ((PCIDevice)device).getConfig();
		return config.getInterruptLine();
	}


}
