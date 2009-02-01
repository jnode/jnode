/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.video.vesa;

import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.Device;
import org.jnode.vm.x86.UnsafeX86;
import org.vmmagic.unboxed.Address;

/**
 * Custom device Mapper for the VESA driver.
 * 
 * @author Levente S\u00e1ntha
 */
public class VESADeviceToDriverMapper implements DeviceToDriverMapper {
    private static final int DISPLAY_CONTROLLER_PCI_DEVICE_CLASS = 0x03;

    public Driver findDriver(Device device) {
        //PCI device needed
        if (!(device instanceof PCIDevice))
            return null;

        //checking display controller device class
        final PCIDevice pciDev = (PCIDevice) device;
        final PCIDeviceConfig cfg = pciDev.getConfig();
        if ((cfg.getBaseClass() & 0xFFFFFF) != DISPLAY_CONTROLLER_PCI_DEVICE_CLASS)
            return null;

        //checking the VESA mode set up by GRUB
        Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
        VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);
        if (vbeInfoBlock.isEmpty())
            return null;

        Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
        ModeInfoBlock modeInfoBlock = new ModeInfoBlock(vbeModeInfo);
        if (modeInfoBlock.isEmpty())
            return null;

        //OK
        return new VESADriver();
    }

    public int getMatchLevel() {
        return DeviceToDriverMapper.MATCH_DEVICE_PREDEFINED;
    }
}
