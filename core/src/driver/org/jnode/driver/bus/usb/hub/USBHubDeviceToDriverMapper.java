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
 
package org.jnode.driver.bus.usb.hub;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBDevice;

/**
 * Device to driver mapper for the USB HUB driver.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBHubDeviceToDriverMapper implements DeviceToDriverMapper, USBConstants {

    /**
     * @see org.jnode.driver.DeviceToDriverMapper#findDriver(org.jnode.driver.Device)
     */
    public Driver findDriver(Device device) {
        if (!(device instanceof USBDevice)) {
            return null;
        }

        final USBDevice dev = (USBDevice) device;
        if (dev.getDescriptor().getDeviceClass() != USB_CLASS_HUB) {
            return null;
        }

        // We found an USB HUB
        return new USBHubDriver();
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
        return MATCH_DEVCLASS;
    }
}
