/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.bus.pci;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.plugin.ConfigurationElement;


/**
 * Generic mapper for PCI devices that match of vendor + device id
 * and an optional revision.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PCIDeviceToDriverMapper extends AbstractPCIDeviceToDriverMapper implements DeviceToDriverMapper {

    public PCIDeviceToDriverMapper(ConfigurationElement config) throws DriverException {
        super(config);
    }

    /**
     * @see org.jnode.driver.DeviceToDriverMapper#findDriver(org.jnode.driver.Device)
     */
    public Driver findDriver(Device device) {
        if (!(device instanceof PCIDevice)) {
            return null;
        }
        final PCIDevice pciDev = (PCIDevice) device;
        final PCIDeviceConfig cfg = pciDev.getConfig();

        if (!matches(cfg.getVendorID(), cfg.getDeviceID(), cfg.getRevision())) {
            return null;
        } else {
            return newDriver(pciDev);
        }
    }

    /**
     * @see org.jnode.driver.DeviceToDriverMapper#getMatchLevel()
     */
    public int getMatchLevel() {
        if (hasMinor()) {
            return DeviceToDriverMapper.MATCH_DEVICE_REVISION;
        } else {
            return DeviceToDriverMapper.MATCH_DEVICE;
        }
    }

}
