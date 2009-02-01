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
 * PCI device configuration header for header type 0: Normal devices.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PCIHeaderType0 extends PCIDeviceConfig {

    private static final int PCI_BASE_ADDRESS_0 = 0x10; /* 32 bits */
    private static final int PCI_BASE_ADDRESS_1 = 0x14; /* 32 bits [htype 0,1 only] */
    private static final int PCI_BASE_ADDRESS_2 = 0x18; /* 32 bits [htype 0 only] */
    private static final int PCI_BASE_ADDRESS_3 = 0x1c; /* 32 bits */
    private static final int PCI_BASE_ADDRESS_4 = 0x20; /* 32 bits */
    private static final int PCI_BASE_ADDRESS_5 = 0x24; /* 32 bits */

    private static final int PCI_CARDBUS_CIS = 0x28;
    private static final int PCI_SUBSYSTEM_VENDOR_ID = 0x2c;
    private static final int PCI_SUBSYSTEM_ID = 0x2e;
    private static final int PCI_ROM_ADDRESS = 0x30; /* Bits 31..11 are address, 10..1 reserved */

    private static final int PCI_INTERRUPT_LINE = 0x3c; // 8 bits
    private static final int PCI_INTERRUPT_PIN = 0x3d; // 8 bits
    private static final int PCI_MIN_GNT = 0x3e; // 8 bits
    private static final int PCI_MAX_LAT = 0x3f; // 8 bits

    /**
     * Rom data
     */
    private final PCIRomAddress romAddress;

    /**
     * @param device
     */
    PCIHeaderType0(PCIDevice device) {
        super(device);
        this.romAddress = PCIRomAddress.read(device, getHeaderType(), PCI_ROM_ADDRESS);
    }

    /**
     * Gets the resource addresses.
     *
     * @return
     */
    public final PCIBaseAddress[] getBaseAddresses() {
        PCIBaseAddress[] addresses = new PCIBaseAddress[6];
        int idx = 0;
        for (int r = 0; r < 6; r++) {
            PCIBaseAddress a = PCIBaseAddress.read(device, PCI_BASE_ADDRESS_0, r);
            if (a != null) {
                addresses[idx++] = a;
                if (a.is64Bit()) {
                    r++;
                }
            }
        }
        if (idx < 6) {
            PCIBaseAddress[] result = new PCIBaseAddress[idx];
            for (int i = 0; i < idx; i++) {
                result[i] = addresses[i];
            }
            return result;
        } else {
            return addresses;
        }
    }

    /**
     * Gets the interrupt pin
     */
    public final int getInterruptPin() {
        return device.readConfigByte(PCI_INTERRUPT_PIN);
    }

    /**
     * Gets the interrupt line
     */
    public final int getInterruptLine() {
        return device.readConfigByte(PCI_INTERRUPT_LINE);
    }

    /**
     * Sets the interrupt line
     *
     * @param line
     */
    public final void setInterruptLine(int line) {
        device.writeConfigByte(PCI_INTERRUPT_LINE, line);
    }

    /**
     * Gets the maximum latency.
     */
    public final int getMaxLatency() {
        return device.readConfigByte(PCI_MAX_LAT);
    }

    /**
     * Gets the ROM base address, or null is no rom is found.
     *
     * @return
     */
    public final PCIRomAddress getRomAddress() {
        return romAddress;
    }

    /**
     * @see org.jnode.driver.bus.pci.PCIDeviceConfig#toString()
     */
    public final String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        if (getInterruptPin() != 0) {
            sb.append(", ");
            sb.append("intr-pin=");
            sb.append(getInterruptPin());
            sb.append(", ");
            sb.append("intr-line=");
            sb.append(getInterruptLine());
        }
        final PCIBaseAddress[] baseAddresses = getBaseAddresses();
        if (baseAddresses.length > 0) {
            sb.append(", base-addresses={");
            for (int i = 0; i < baseAddresses.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(baseAddresses[i]);
            }
            sb.append('}');
        }
        if (romAddress != null) {
            sb.append(", rom=");
            sb.append(romAddress);
        }
        return sb.toString();
    }
}
