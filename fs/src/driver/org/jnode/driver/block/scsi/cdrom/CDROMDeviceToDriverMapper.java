/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.driver.block.scsi.cdrom;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.scsi.SCSIDevice;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDROMDeviceToDriverMapper implements DeviceToDriverMapper {

    /**
     * @see org.jnode.driver.DeviceToDriverMapper#findDriver(org.jnode.driver.Device)
     */
    public Driver findDriver(Device device) {
        if (device instanceof SCSIDevice) {
            final SCSIDevice dev = (SCSIDevice) device;
            if (dev.getDescriptor().getPeripheralDeviceType() == 0x05) {
                // CD/DVD device
                return new SCSICDROMDriver();
            }
        }
        return null;
    }

    /**
     * @see org.jnode.driver.DeviceToDriverMapper#getMatchLevel()
     */
    public int getMatchLevel() {
        return DeviceToDriverMapper.MATCH_DEVCLASS;
    }
}
