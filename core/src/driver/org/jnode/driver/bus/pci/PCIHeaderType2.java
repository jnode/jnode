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

package org.jnode.driver.bus.pci;

/**
 * PCI device configuration header for header type 2: Cardbus bridges.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PCIHeaderType2 extends PCIDeviceConfig {

    /* Header type 2 (CardBus bridges) */
    public static final int PCI_CB_CAPABILITY_LIST = 0x14;
    /* = 0x15 reserved */
    public static final int PCI_CB_SEC_STATUS = 0x16; /* Secondary status */
    public static final int PCI_CB_PRIMARY_BUS = 0x18; /* PCI bus number */
    public static final int PCI_CB_CARD_BUS = 0x19; /* CardBus bus number */
    public static final int PCI_CB_SUBORDINATE_BUS = 0x1a; /* Subordinate bus number */
    public static final int PCI_CB_LATENCY_TIMER = 0x1b; /* CardBus latency timer */
    public static final int PCI_CB_MEMORY_BASE_0 = 0x1c;
    public static final int PCI_CB_MEMORY_LIMIT_0 = 0x20;
    public static final int PCI_CB_MEMORY_BASE_1 = 0x24;
    public static final int PCI_CB_MEMORY_LIMIT_1 = 0x28;
    public static final int PCI_CB_IO_BASE_0 = 0x2c;
    public static final int PCI_CB_IO_BASE_0_HI = 0x2e;
    public static final int PCI_CB_IO_LIMIT_0 = 0x30;
    public static final int PCI_CB_IO_LIMIT_0_HI = 0x32;
    public static final int PCI_CB_IO_BASE_1 = 0x34;
    public static final int PCI_CB_IO_BASE_1_HI = 0x36;
    public static final int PCI_CB_IO_LIMIT_1 = 0x38;
    public static final int PCI_CB_IO_LIMIT_1_HI = 0x3a;
    public static final long PCI_CB_IO_RANGE_MASK = (~0x03L);
    /* = 0x3c-= 0x3d are same as for htype 0 */
    public static final int PCI_CB_BRIDGE_CONTROL = 0x3e;
    public static final int PCI_CB_BRIDGE_CTL_PARITY = 0x01; /*
                                                              * Similar to standard bridge control
                                                              * register
                                                              */
    public static final int PCI_CB_BRIDGE_CTL_SERR = 0x02;
    public static final int PCI_CB_BRIDGE_CTL_ISA = 0x04;
    public static final int PCI_CB_BRIDGE_CTL_VGA = 0x08;
    public static final int PCI_CB_BRIDGE_CTL_MASTER_ABORT = 0x20;
    public static final int PCI_CB_BRIDGE_CTL_CB_RESET = 0x40; /* CardBus reset */
    public static final int PCI_CB_BRIDGE_CTL_16BIT_INT = 0x80; /*
                                                                 * Enable interrupt for 16-bit
                                                                 * cards
                                                                 */
    public static final int PCI_CB_BRIDGE_CTL_PREFETCH_MEM0 = 0x100; /*
                                                                      * Prefetch enable for both
                                                                      * memory regions
                                                                      */
    public static final int PCI_CB_BRIDGE_CTL_PREFETCH_MEM1 = 0x200;
    public static final int PCI_CB_BRIDGE_CTL_POST_WRITES = 0x400;
    public static final int PCI_CB_SUBSYSTEM_VENDOR_ID = 0x40;
    public static final int PCI_CB_SUBSYSTEM_ID = 0x42;
    public static final int PCI_CB_LEGACY_MODE_BASE = 0x44; /*
                                                             * 16-bit PC Card legacy mode base
                                                             * address (ExCa)
                                                             */
    /* = 0x48-= 0x7f reserved */

    /**
     * @param device
     */
    PCIHeaderType2(PCIDevice device) {
        super(device);
    }

    public final int getPrimaryBus() {
        return device.readConfigByte(PCI_CB_PRIMARY_BUS);
    }

    public final int getCardBus() {
        return device.readConfigByte(PCI_CB_CARD_BUS);
    }

    public final int getSubordinateBus() {
        return device.readConfigByte(PCI_CB_SUBORDINATE_BUS);
    }

    /**
     * @see org.jnode.driver.pci.PCIDeviceConfig#toString()
     */
    public String toString() {
        return super.toString() + ", " + "primary-bus=" + getPrimaryBus()
            + ", " + "card-bus=" + getCardBus()
            + ", " + "subordinate-bus=" + getSubordinateBus();
    }
}
