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
 
package org.jnode.driver.bus.pci;

/**
 * PCI device configuration header for header type 1: PCI-PCI bridge.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PCIHeaderType1 extends PCIDeviceConfig {

    /* Header type 1 (PCI-to-PCI bridges) */
    private static final int PCI_PRIMARY_BUS = 0x18; /* Primary bus number */

    private static final int PCI_SECONDARY_BUS = 0x19; /* Secondary bus number */

    private static final int PCI_SUBORDINATE_BUS = 0x1a; /*
                                                         * Highest bus number
                                                         * behind the bridge
                                                         */

    private static final int PCI_SEC_LATENCY_TIMER = 0x1b; /*
                                                             * Latency timer for
                                                             * secondary
                                                             * interface
                                                             */

    private static final int PCI_IO_BASE = 0x1c; /* I/O range behind the bridge */

    private static final int PCI_IO_LIMIT = 0x1d;

    private static final long PCI_IO_RANGE_TYPE_MASK = 0x0fL; /*
                                                                 * I/O bridging
                                                                 * type
                                                                 */

    private static final int PCI_IO_RANGE_TYPE_16 = 0x00;

    private static final int PCI_IO_RANGE_TYPE_32 = 0x01;

    private static final long PCI_IO_RANGE_MASK = (~0x0fl);

    private static final int PCI_SEC_STATUS = 0x1e; /*
                                                     * Secondary status
                                                     * register, only bit 14
                                                     * used
                                                     */

    private static final int PCI_MEMORY_BASE = 0x20; /* Memory range behind */

    private static final int PCI_MEMORY_LIMIT = 0x22;

    private static final long PCI_MEMORY_RANGE_TYPE_MASK = 0x0fL;

    private static final long PCI_MEMORY_RANGE_MASK = (~0x0fL);

    private static final int PCI_PREF_MEMORY_BASE = 0x24; /*
                                                             * Prefetchable
                                                             * memory range
                                                             * behind
                                                             */

    private static final int PCI_PREF_MEMORY_LIMIT = 0x26;

    private static final long PCI_PREF_RANGE_TYPE_MASK = 0x0fL;

    private static final int PCI_PREF_RANGE_TYPE_32 = 0x00;

    private static final int PCI_PREF_RANGE_TYPE_64 = 0x01;

    private static final long PCI_PREF_RANGE_MASK = (~0x0fL);

    private static final int PCI_PREF_BASE_UPPER32 = 0x28; /*
                                                             * Upper half of
                                                             * prefetchable
                                                             * memory range
                                                             */

    private static final int PCI_PREF_LIMIT_UPPER32 = 0x2c;

    private static final int PCI_IO_BASE_UPPER16 = 0x30; /*
                                                         * Upper half of I/O
                                                         * addresses
                                                         */

    private static final int PCI_IO_LIMIT_UPPER16 = 0x32;

    /* = 0x34 same as for htype 0 */
    /* = 0x35-= 0x3b is reserved */
    private static final int PCI_ROM_ADDRESS = 0x38;

    /* = 0x3c-= 0x3d are same as for htype 0 */
    private static final int PCI_BRIDGE_CONTROL = 0x3e;

    private static final int PCI_BRIDGE_CTL_PARITY = 0x01; /*
                                                             * Enable parity
                                                             * detection on
                                                             * secondary
                                                             * interface
                                                             */

    private static final int PCI_BRIDGE_CTL_SERR = 0x02; /*
                                                         * The same for SERR
                                                         * forwarding
                                                         */

    private static final int PCI_BRIDGE_CTL_NO_ISA = 0x04; /*
                                                             * Disable bridging
                                                             * of ISA ports
                                                             */

    private static final int PCI_BRIDGE_CTL_VGA = 0x08; /* Forward VGA addresses */

    private static final int PCI_BRIDGE_CTL_MASTER_ABORT = 0x20; /*
                                                                 * Report master
                                                                 * aborts
                                                                 */

    private static final int PCI_BRIDGE_CTL_BUS_RESET = 0x40; /*
                                                                 * Secondary bus
                                                                 * reset
                                                                 */

    private static final int PCI_BRIDGE_CTL_FAST_BACK = 0x80; /*
                                                                 * Fast
                                                                 * Back2Back
                                                                 * enabled on
                                                                 * secondary
                                                                 * interface
                                                                 */

    /**
     * @param device
     */
    PCIHeaderType1(PCIDevice device) {
        super(device);
    }

    /**
     * Gets the primary bus number. Only valid for bridge devices.
     */
    public final int getPrimaryBus() {
        return device.readConfigByte(PCI_PRIMARY_BUS);
    }

    /**
     * Gets the secondary bus number. Only valid for bridge devices.
     */
    public final int getSecondaryBus() {
        return device.readConfigByte(PCI_SECONDARY_BUS);
    }

    public final int getSubordinateBus() {
        return device.readConfigByte(PCI_SUBORDINATE_BUS);
    }

    /**
     * @see org.jnode.driver.pci.PCIDeviceConfig#toString()
     */
    public String toString() {
        return super.toString() + ", " + "primary-bus=" + getPrimaryBus()
            + ", " + "secondary-bus=" + getSecondaryBus()
            + ", " + "subordinate-bus=" + getSubordinateBus();
    }

}
