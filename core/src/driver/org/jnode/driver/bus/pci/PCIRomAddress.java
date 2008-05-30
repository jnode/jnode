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

import org.jnode.util.NumberUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PCIRomAddress {

    private static final int PCI_ROM_ADDRESS_ENABLE = 0x01;
    private static final int PCI_ROM_ADDRESS_MASK = (~0x7ff);

    /**
     * The device
     */
    private final PCIDevice dev;
    /**
     * The offset of the rom address
     */
    private final int offset;
    /**
     * Size of the address space
     */
    private final int size;

    final static PCIRomAddress read(PCIDevice dev, int headerType, int offset) {
        final int rawData = dev.readConfigDword(offset);
        // Determine the size
        dev.writeConfigDword(offset, ~PCI_ROM_ADDRESS_ENABLE);
        final int sizeData = dev.readConfigDword(offset);
        // Restore previous data
        dev.writeConfigDword(offset, rawData);

        if ((sizeData == 0) || (sizeData == 0xFFFFFFFF)) {
            return null;
        } else {
            return new PCIRomAddress(dev, offset, rawData, sizeData);
        }
    }

    private static int pci_size(int base, int mask) {
        int size = mask & base;
        size = size & ~(size - 1);
        return size;
    }

    /**
     * Create a new instance
     *
     * @param rawData
     */
    private PCIRomAddress(PCIDevice dev, int offset, int rawData, int sizeData) {
        this.dev = dev;
        this.offset = offset;
        this.size = pci_size(sizeData, PCI_ROM_ADDRESS_MASK);
    }

    /**
     * Gets the starting memory address
     */
    public final int getRomBase() {
        return dev.readConfigDword(offset) & PCI_ROM_ADDRESS_MASK;
    }

    /**
     * Gets the size of the IO or memory space
     */
    public final int getSize() {
        return size;
    }

    /**
     * Is the ROM base address enabled.
     *
     * @return
     */
    public final boolean isEnabled() {
        return ((dev.readConfigDword(offset) & PCI_ROM_ADDRESS_ENABLE) != 0);
    }

    /**
     * Enable/disable the ROM
     */
    public final void setEnabled(boolean enabled) {
        int v = dev.readConfigDword(offset);
        if (enabled) {
            v |= PCI_ROM_ADDRESS_ENABLE;
        } else {
            v &= ~PCI_ROM_ADDRESS_ENABLE;
        }
        dev.writeConfigDword(offset, v);
    }

    /**
     * Convert this to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final int base = getRomBase();
        return NumberUtils.hex(getRomBase()) + "-" + NumberUtils.hex(base + size - 1) +
            (isEnabled() ? " enabled" : " disabled");
    }
}
