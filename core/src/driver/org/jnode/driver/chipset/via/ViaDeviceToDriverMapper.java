/*
 * $Id$
 */
package org.jnode.driver.chipset.via;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.driver.pci.PCI_IDs;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ViaDeviceToDriverMapper implements DeviceToDriverMapper {

	/**
	 * Try to find a driver for the given device.
	 * @param device
	 * @see org.jnode.driver.DeviceToDriverMapper#findDriver(org.jnode.driver.Device)
	 * @return The driver
	 */
	public Driver findDriver(Device device) {
		if (!(device instanceof PCIDevice)) {
			return null;
		}
		final PCIDevice dev = (PCIDevice) device;
		final PCIDeviceConfig config = dev.getConfig();

		if (config.getVendorID() != PCI_IDs.PCI_VENDOR_ID_VIATEC) {
			return null;
		}

		switch (config.getDeviceID()) {
			case 0x0305:
				return new Via8363_0();
			case 0x0686 :
				return new Via82C686();
			default:
				return null;
		}
	}
	
	/**
	 * Gets the matching level of this mapper.
	 * The mappers are queried in order of match level. This will ensure
	 * the best available driver for a device.
	 * 
	 * @return One of the MATCH_xxx constants.
	 * @see #MATCH_DEVICE_REVISION
	 * @see #MATCH_DEVICE
	 * @see #MATCH_DEVCLASS
	 */
	public int getMatchLevel() {
		return MATCH_DEVICE;
	}
}
