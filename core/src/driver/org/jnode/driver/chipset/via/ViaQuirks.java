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
 
package org.jnode.driver.chipset.via;

import org.apache.log4j.Logger;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCI_IDs;

/**
 * Various fixed for the Via chipset.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ViaQuirks {

    /**
     * Lantency fix with the system contains a buggy southbridge.
     *
     * @param dev
     * @param log
     */
    public static void applyLatencyFix(PCIDevice dev, Logger log) {

        PCIDevice d = dev.getPCIBusAPI().findDevice(PCI_IDs.PCI_VENDOR_ID_VIATEC, 0x686);
        if (d != null) {
            // For revision 0x40-0x42 we have a buggy southbridge.
            final int rev = d.getConfig().getRevision();

            if ((rev >= 0x40) && (rev <= 0x42)) {
                /*
                     * Ok we have the problem. Now set the PCI master grant to occur every master
                     * grant. The apparent bug is that under high PCI load (quite common in Linux of
                     * course) you can get data loss when the CPU is held off the bus for 3 bus master
                     * requests This happens to include the IDE controllers....
                     *
                     * VIA only apply this fix when an SB Live! is present but under both Linux and
                     * Windows this isnt enough, and we have seen corruption without SB Live! but with
                     * things like 3 UDMA IDE controllers. So we ignore that bit of the VIA
                     * recommendation..
                     */

                int busarb = dev.readConfigByte(0x76);
                /*
                     * Set bit 4 and bi 5 of byte 76 to 0x01
                     */
                busarb &= ~(1 << 5);
                busarb |= (1 << 4);
                dev.writeConfigByte(0x76, busarb);
                log.debug("Applying VIA southbridge workaround on device " + dev.getPCIName());
            }
        }
    }
}
