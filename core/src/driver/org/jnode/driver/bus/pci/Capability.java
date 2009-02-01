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

import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Capability {

    /**
     * Offset of this capability in the device config space
     */
    private final int offset;
    /**
     * The device this is a capability of
     */
    private final PCIDevice device;
    /**
     * The capability id
     */
    private final int id;

    /**
     * Well known capability id's.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class Id {
        /**
         * Power Management
         */
        public static final int PM = 0x01;
        /**
         * Accelerated Graphics Port
         */
        public static final int AGP = 0x02;
        /**
         * Vital Product Data
         */
        public static final int VPD = 0x03;
        /**
         * Slot Identification
         */
        public static final int SLOTID = 0x04;
        /**
         * Message Signalled Interrupts
         */
        public static final int MSI = 0x05;
        /**
         * CompactPCI HotSwap
         */
        public static final int CHSWP = 0x06;
    }

    private static final int PCI_CAP_LIST_ID = 0; /* Capability ID */
    private static final int PCI_CAP_LIST_NEXT = 1; /* Next capability in the list */
    private static final int PCI_CAP_FLAGS = 2; /* Capability defined flags (16 bits) */
    private static final int PCI_CAP_SIZEOF = 4;

    /**
     * Initialize this instance.
     *
     * @param device
     * @param offset
     */
    Capability(PCIDevice device, int offset) {
        this.device = device;
        this.offset = offset;
        this.id = device.readConfigByte(offset + PCI_CAP_LIST_ID);
    }

    /**
     * Create a concrete instance of Capability, based on the
     * id read from the PCI config space.
     *
     * @param device
     * @param offset
     * @return
     */
    static final Capability createCapability(PCIDevice device, int offset) {
        final int id = device.readConfigByte(offset + PCI_CAP_LIST_ID);
        switch (id) {
            case Id.PM:
                return new PMCapability(device, offset);
            case Id.AGP:
                return new AGPCapability(device, offset);
            case Id.VPD:
                return new VPDCapability(device, offset);
            case Id.SLOTID:
                return new SlotIDCapability(device, offset);
            case Id.MSI:
                return new MSICapability(device, offset);
            case Id.CHSWP:
                return new CompactHotSwapCapability(device, offset);
            default:
                return new Capability(device, offset) {
                };
        }
    }

    /**
     * Gets the capability ID.
     *
     * @return
     */
    public final int getId() {
        return id;
    }

    /**
     * Convert to a string representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id=0x");
        sb.append(NumberUtils.hex(getId(), 2));

        return sb.toString();
    }

    /**
     * Read a configuration dword for this device at a given offset
     *
     * @param offset
     */
    protected final int readConfigDword(int offset) {
        return device.readConfigDword(this.offset + offset);
    }

    /**
     * Read a configuration word for this device at a given offset
     *
     * @param offset
     */
    protected final int readConfigWord(int offset) {
        return device.readConfigWord(this.offset + offset);
    }

    /**
     * Read a configuration byte for this device at a given offset
     *
     * @param offset
     */
    protected final int readConfigByte(int offset) {
        return device.readConfigByte(this.offset + offset);
    }

    /**
     * Write a configuration dword for this device at a given offset
     *
     * @param offset
     * @param value
     */
    protected final void writeConfigDword(int offset, int value) {
        device.writeConfigDword(this.offset + offset, value);
    }

    /**
     * Write a configuration word for this device at a given offset
     *
     * @param offset
     * @param value
     */
    protected final void writeConfigWord(int offset, int value) {
        device.writeConfigWord(this.offset + offset, value);
    }

    /**
     * Write a configuration byte for this device at a given offset
     *
     * @param offset
     * @param value
     */
    protected final void writeConfigByte(int offset, int value) {
        device.writeConfigByte(this.offset + offset, value);
    }
}
