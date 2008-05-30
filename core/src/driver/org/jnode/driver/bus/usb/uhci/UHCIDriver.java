/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.bus.usb.uhci;

import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.usb.AbstractHostControllerDriver;
import org.jnode.driver.bus.usb.USBHostControllerAPI;
import org.jnode.system.ResourceNotFreeException;

/**
 * UHCI (Universal Host Controller Interface) driver.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIDriver extends AbstractHostControllerDriver {

    /**
     * The low-level implementation
     */
    private UHCICore core;

    /**
     * Initialize this instance
     */
    public UHCIDriver() {
    }

    /**
     * @see org.jnode.driver.bus.usb.AbstractHostControllerDriver#claimResources()
     */
    protected void claimResources() throws DriverException {
        try {
            core = new UHCICore((PCIDevice) getDevice());
        } catch (ResourceNotFreeException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * @see org.jnode.driver.bus.usb.AbstractHostControllerDriver#releaseResources()
     */
    protected void releaseResources() {
        core.release();
        core = null;
    }

    /**
     * Gets the API implementation.
     */
    protected USBHostControllerAPI getAPIImplementation() {
        return core;
    }

    /**
     * Gets the prefix for the device name
     */
    protected String getDevicePrefix() {
        return "usb-uhci";
    }
}
