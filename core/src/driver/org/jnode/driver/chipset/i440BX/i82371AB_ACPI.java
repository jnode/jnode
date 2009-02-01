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
 
package org.jnode.driver.chipset.i440BX;

import org.jnode.driver.Driver;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.smbus.SMBus;
import org.jnode.driver.bus.smbus.SMBusControler;

/**
 * i82371AB_ACPI.
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

public class i82371AB_ACPI extends Driver {

    PCIDevice device = null;
    SMBus smbus = null;
    SMBusControler smbControler = null;

    public i82371AB_ACPI() {
    }

    protected void stopDevice() throws org.jnode.driver.DriverException {
        /** @todo Implement this org.jnode.driver.Driver abstract method */
    }

    protected void startDevice() throws org.jnode.driver.DriverException {
        device = (PCIDevice) getDevice();

        //int revision = device.readConfigByte(0x8) & 0xff;
        //int countb = device.readConfigByte(0x48);
        //int pmbase = device.readConfigDword(0x40) & 0xffC0; // just use bits 15-6 other bits are
        // reserved and should be 0 except bit
        // 0,
        // bit 0 is hardwired to 1 to show the address is system IO
        //int pmregenabled = device.readConfigByte(0x80);

        // log.info("i82371AB revision " + revision + ", ACPI configuration: enabled(" +
        // (pmregenabled !=0 ? true : false) + ") pmbase(" + Integer.toHexString(pmbase) + ")
        // countb(" +Integer.toHexString(countb)+")");

        smbControler = new i82371AB_ACPI_SMBusControler(device);
        smbus = new SMBus(device, smbControler);
        smbus.probeDevices();
    }

}
