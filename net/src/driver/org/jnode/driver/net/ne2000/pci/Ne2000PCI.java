/*
 * $Id$
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
