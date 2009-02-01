/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.driver.net.ne2000.pci;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.net.ne2000.Ne2000Core;
import org.jnode.driver.net.ne2000.Ne2000Driver;
import org.jnode.driver.net.ne2000.Ne2000Flags;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class Ne2000PCIDriver extends Ne2000Driver {

    /**
     * Create a new instance
     */
    public Ne2000PCIDriver(ConfigurationElement config) {
        super(new Ne2000Flags(config));
    }

    /**
     * Create a new instance
     * 
     * @param flags
     */
    public Ne2000PCIDriver(Ne2000Flags flags) {
        super(flags);
    }

    /**
     * Create a new Ne2000Core instance
     * 
     * @see org.jnode.driver.net.ne2000.Ne2000Driver#newCore(org.jnode.driver.Device,
     *      org.jnode.driver.net.ne2000.Ne2000Flags)
     */
    protected Ne2000Core newCore(Device device, Ne2000Flags flags)
        throws DriverException, ResourceNotFreeException {
        return new Ne2000PCI(this, device, (PCIDevice) device, flags);
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
