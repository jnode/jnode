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
 
package org.jnode.driver.bus.pci;

import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public final class PCIBaseAddress {

    private static final int PCI_BASE_ADDRESS_SPACE = 0x01; /* 0 = memory, 1 = I/O */
    private static final int PCI_BASE_ADDRESS_SPACE_IO = 0x01;
    private static final int PCI_BASE_ADDRESS_SPACE_MEMORY = 0x00;
    private static final int PCI_BASE_ADDRESS_MEM_TYPE_MASK = 0x06;
    private static final int PCI_BASE_ADDRESS_MEM_TYPE_32 = 0x00; /* 32 bit address */
    private static final int PCI_BASE_ADDRESS_MEM_TYPE_1M = 0x02; /* Below 1M [obsolete] */
    private static final int PCI_BASE_ADDRESS_MEM_TYPE_64 = 0x04; /* 64 bit address */
    private static final int PCI_BASE_ADDRESS_MEM_PREFETCH = 0x08; /* prefetchable? */
    private static final long PCI_BASE_ADDRESS_MEM_MASK = (~0x0fL);
    private static final long PCI_BASE_ADDRESS_IO_MASK = (~0x03L);

    /**
     * Is this base address in IO Space?
     */
    private final boolean isIO;
    /**
     * The base address in the IO space
     */
    private final int ioAddress;
    /**
     * Memory type
     */
    private final byte memType;
    /**
     * The base address in the memory space
     */
    private final long memAddress;
    /**
     * Must a memory address be relocated below 1Mb?
     */
    private final boolean below1Mb;
    /**
     * Is this a 64-bit memory address?
     */
    private final boolean b64;
    /**
     * Size of the address space
     */
    private final int size;

    /**
     * Read a base address at a given index (0..5)
     *
     * @param dev
     * @param index
     * @return
     */
    public static PCIBaseAddress read(PCIDevice dev, int address0Offset, int index) {
        if ((index < 0) || (index > 5)) {
            throw new IllegalArgumentException("index out or range " + index);
        }

        final int ofs = address0Offset + (index << 2);
        final int rawData = dev.readConfigDword(ofs);
        // Determine the size
        dev.writeConfigDword(ofs, 0xFFFFFFFF);
        final int sizeData = dev.readConfigDword(ofs);
        // Restore previous data
        dev.writeConfigDword(ofs, rawData);

        if ((sizeData == 0) || (sizeData == 0xFFFFFFFF)) {
            return null;
        } else {
            return new PCIBaseAddress(dev, ofs, rawData, sizeData);
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
    private PCIBaseAddress(PCIDevice dev, int ofs, int rawData, int sizeData) {

        this.isIO = ((rawData & 0x01) != 0);
        if (isIO) {
            this.ioAddress = rawData & 0xFFFFFFFC;
            this.size = pci_size(sizeData, 0xFFFFFFFC);
            // Set dummy values to memory specific fields
            this.memType = 0;
            this.memAddress = 0;
            this.below1Mb = false;
            this.b64 = false;
        } else {
            this.memType = (byte) ((rawData >> 1) & 0x03);
            this.size = pci_size(sizeData, 0xFFFFFFF0);
            switch (memType) {
                case 0x00: {
                    // 32-bit address relocatable anywhere in the memory space
                    this.memAddress = rawData & 0xFFFFFFF0;
                    this.below1Mb = false;
                    this.b64 = false;
                    break;
                }
                case 0x01: {
                    // 32-bit address relocatable below 1Mb boundary
                    this.memAddress = rawData & 0xFFFFFFF0;
                    this.below1Mb = true;
                    this.b64 = false;
                    break;
                }
                case 0x02: {
                    // 64-bit address relocatable anywhere in the memory space
                    final int nextRawData = dev.readConfigDword(ofs + 4);
                    this.memAddress = ((rawData & 0xFFFFFFF0) | (nextRawData << 32));
                    this.below1Mb = false;
                    this.b64 = true;
                    break;
                }
                default: {
                    // Unknown type
                    this.memAddress = -1;
                    this.below1Mb = false;
                    this.b64 = false;
                }
            }
            // Set dummy values to io specific fields
            this.ioAddress = -1;
        }
    }

    /**
     * Is this base address in IO space?
     */
    public boolean isIOSpace() {
        return isIO;
    }

    /**
     * Is this base address in memory space.
     */
    public boolean isMemorySpace() {
        return !isIO;
    }

    /**
     * Is this a valid base address?
     */
    public boolean isValid() {
        return (isIO || ((memType >= 0) && (memType <= 2)));
    }

    /**
     * Is this a 64-bit address?
     */
    public boolean is64Bit() {
        return b64;
    }

    /**
     * Is this a 32-bit address?
     */
    public boolean is32Bit() {
        return !b64;
    }

    /**
     * Must this address be relocated below the 1Mb boundary?
     */
    public boolean isBelow1Mb() {
        return below1Mb;
    }

    /**
     * Gets the staring IO address
     */
    public int getIOBase() {
        return ioAddress;
    }

    /**
     * Gets the starting memory address
     */
    public long getMemoryBase() {
        return memAddress;
    }

    /**
     * Gets the size of the IO or memory space
     */
    public int getSize() {
        return size;
    }

    /**
     * Convert this to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (!isValid()) {
            return "Invalid";
        } else if (isIO) {
            return "IO:" + NumberUtils.hex(ioAddress) + "-" + NumberUtils.hex(ioAddress + size - 1);
        } else if (b64) {
            return "MEM64:" + NumberUtils.hex(memAddress) + "-" + NumberUtils.hex(memAddress + size - 1);
        } else if (below1Mb) {
            return "MEM32-BELOW1Mb:" + NumberUtils.hex((int) memAddress) + "-" +
                NumberUtils.hex((int) (memAddress + size - 1));
        } else {
            return "MEM32:" + NumberUtils.hex((int) memAddress) + "-" + NumberUtils.hex((int) (memAddress + size - 1));
        }
    }
}
