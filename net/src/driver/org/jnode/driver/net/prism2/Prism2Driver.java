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
 
package org.jnode.driver.net.prism2;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.wireless.spi.WirelessDeviceCore;
import org.jnode.driver.net.wireless.spi.WirelessEthernetDriver;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

public class Prism2Driver extends WirelessEthernetDriver {
    
    /**
     * Create new driver instance for this device
     */
    public Prism2Driver(ConfigurationElement config) {
        this(new Prism2Flags(config));
    }

    /**
     * Create new driver instance for this device
     * 
     * @param flags
     */
    public Prism2Driver(Prism2Flags flags) {
        this.flags = flags;
    }

    /**
     * Create a new Prism2Core instance
     */
    protected WirelessDeviceCore newWirelessCore(Device device, Flags flags)
            throws DriverException, ResourceNotFreeException {
        return new Prism2Core(this, device, (PCIDevice) device, flags);
    }

}
