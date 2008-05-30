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

package org.jnode.driver.chipset.i440BX;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.bus.pci.PCI_IDs;

/**
 * i440BX device to driver mapper.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class i440BXDeviceToDriverMapper implements DeviceToDriverMapper {

    //private static Firmware firmware;

    public Driver findDriver(Device device) {
        if (!(device instanceof PCIDevice)) {
            return null;
        }
        final PCIDevice dev = (PCIDevice) device;
        final PCIDeviceConfig config = dev.getConfig();

        if (config.getVendorID() != PCI_IDs.PCI_VENDOR_ID_INTEL)
            return null;

        switch (config.getDeviceID()) {

            // 440BX chipset members
            case 0x7110:
                //firmware=new BIOS(); // we assume this is a BIOS system, not an EFI
                //return new i82371AB_ISABridge();
                break;
            case 0x7111:
                //return new i82371AB_IDEController();
                break;
            case 0x7112:
                //return new i82371AB_USBController();
                break;
            case 0x7113:
                return new i82371AB_ACPI();
            case 0x7190:
                //return new i82443BX_HostPCIBridge();
                break;
            case 0x7191:
                //return new i82443BX_PCIPCIBridge();
                break;
            case 0x7192:
                // return new i82443BX_HostPCIBridge();
                break;

            default:
                return null;
        }

        return null;
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
