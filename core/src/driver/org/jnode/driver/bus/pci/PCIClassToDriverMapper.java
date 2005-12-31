/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.driver.bus.pci;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.plugin.ConfigurationElement;


/**
 * Generic mapper for PCI devices that match of baseclass + subclass 
 * and an optional minor class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PCIClassToDriverMapper extends AbstractPCIDeviceToDriverMapper implements DeviceToDriverMapper {

    /**
     * @param config
     * @throws DriverException
     */
    public PCIClassToDriverMapper(ConfigurationElement config)
            throws DriverException {
        super(config);
    }
    
    /**
     * @see org.jnode.driver.DeviceToDriverMapper#findDriver(org.jnode.driver.Device)
     */
    public Driver findDriver(Device device) {
        if (!(device instanceof PCIDevice)) {
            return null;
        }
        final PCIDevice pciDev = (PCIDevice)device;
        final PCIDeviceConfig cfg = pciDev.getConfig();
        
        if (!matches(cfg.getBaseClass(), cfg.getSubClass(), cfg.getMinorClass())) {
            return null;
        } else {
            return newDriver(pciDev);
        }
    }
    
    /**
     * @see org.jnode.driver.DeviceToDriverMapper#getMatchLevel()
     */
    public int getMatchLevel() {
        return DeviceToDriverMapper.MATCH_DEVCLASS;
    }
}
