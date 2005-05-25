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
 
package org.jnode.driver.chipset.via;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.bus.pci.PCI_IDs;

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
