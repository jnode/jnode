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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public abstract class PCIDeviceConfig implements PCIConstants {

    private static final int PCI_VENDOR_ID = 0x00; /* 16 bits */
    private static final int PCI_DEVICE_ID = 0x02; /* 16 bits */
    private static final int PCI_COMMAND = 0x04; /* 16 bits */
    private static final int PCI_STATUS = 0x06; /* 16 bits */
    private static final int PCI_CLASS_REVISION = 0x08; // High 24 bits are class, low 8
    private static final int PCI_REVISION_ID = 0x08; /* Revision ID */
    private static final int PCI_CLASS_PROG = 0x09; /* Reg. Level Programming Interface */
    private static final int PCI_CLASS_DEVICE = 0x0a; /* Device class */
    private static final int PCI_CACHE_LINE_SIZE = 0x0c; /* 8 bits */
    private static final int PCI_LATENCY_TIMER = 0x0d; /* 8 bits */
    private static final int PCI_HEADER_TYPE = 0x0e; /* 8 bits */
    private static final int PCI_BIST = 0x0f; /* 8 bits */
    private static final int PCI_CAPABILITY_LIST = 0x34; /* Offset of first capability list entry */

    /**
     * My device
     */
    protected final PCIDevice device;
    /**
     * My device ID
     */
    private final int deviceID;
    /**
     * The vendor ID
     */
    private final int vendorID;
    /**
     * The major class
     */
    private final int majorClass;
    /**
     * The sub class
     */
    private final int subClass;
    /**
     * The minor class
     */
    private int minorClass;
    /**
     * The revision
     */
    private final int revision;
    /**
     * The header type
     */
    private final int headerTypeRaw;
    /**
     * The list of capabilities
     */
    private Map<Integer, Capability> capabilities;

    /**
     * Create a new instance
     *
     * @param device
     */
    protected PCIDeviceConfig(PCIDevice device) {
        this.device = device;
        this.deviceID = device.readConfigWord(PCI_DEVICE_ID);
        this.vendorID = device.readConfigWord(PCI_VENDOR_ID);
        int reg2 = device.readConfigDword(PCI_CLASS_REVISION);
        this.majorClass = (reg2 >> 24) & 0xFF;
        this.subClass = (reg2 >> 16) & 0xFF;
        this.minorClass = (reg2 >> 8) & 0xFF;
        this.revision = (reg2 & 0xFF);
        this.headerTypeRaw = device.readConfigByte(PCI_HEADER_TYPE);
    }

    /**
     * Create the correct device config for the given device,
     * based on the header type of the device configuration settings.
     *
     * @param device
     * @return
     */
    static final PCIDeviceConfig createConfig(PCIDevice device) {
        final int headerTypeRaw = device.readConfigByte(PCI_HEADER_TYPE);
        final int headerType = headerTypeRaw & 0x7F;
        switch (headerType) {
            case HEADER_TYPE_NORMAL:
                return new PCIHeaderType0(device);
            case HEADER_TYPE_BRIDGE:
                return new PCIHeaderType1(device);
            case HEADER_TYPE_CARDBUS:
                return new PCIHeaderType2(device);
            default:
                return new PCIDeviceConfig(device) {
                };
        }
    }

    /**
     * Gets the identifier of this unit
     */
    public final int getDeviceID() {
        return deviceID;
    }

    /**
     * Gets the ID of the device at the given location.
     *
     * @param pci
     * @param bus
     * @param unit
     * @param func
     * @return
     */
    static final int getDeviceID(PCIDriver pci, int bus, int unit, int func) {
        return pci.readConfigWord(bus, unit, func, PCI_DEVICE_ID);
    }

    /**
     * Is this unit present
     */
    public final boolean isPresent() {
        final int devID = getDeviceID();
        switch (devID) {
            case 0xFFFF:
                return false;
            case 0:
                return (device.getFunction() == 0);
            default:
                return true;
        }
    }

    /**
     * Is a device at a given location present?
     */
    static final boolean isPresent(PCIDriver pci, int bus, int unit, int func) {
        final int devID = getDeviceID(pci, bus, unit, func);
        switch (devID) {
            case 0xFFFF:
                return false;
            case 0:
                return (func == 0);
            default:
                return true;
        }
    }

    /**
     * Gets the PCI class information of a device at a given location
     *
     * @param pci
     * @param bus
     * @param unit
     * @param func
     * @return { major, sub, minor, revision }
     */
    static final int[] getPCIClass(PCIDriver pci, int bus, int unit, int func) {
        final int reg2 = pci.readConfigDword(bus, unit, func, PCI_CLASS_REVISION);
        final int[] rc = new int[4];
        rc[0] = (reg2 >> 24) & 0xFF;
        rc[1] = (reg2 >> 16) & 0xFF;
        rc[2] = (reg2 >> 8) & 0xFF;
        rc[3] = (reg2 & 0xFF);
        return rc;
    }

    /**
     * Gets the identifier of the vendor of this unit
     */
    public final int getVendorID() {
        return vendorID;
    }

    /**
     * Gets the descriptor of the vendor of this unit
     */
    public final VendorDescriptor getVendorDescriptor() {
        return PCIDescriptors.getInstance().findVendor(getVendorID());
    }

    /**
     * Gets the descriptor of this device.
     */
    public final DeviceDescriptor getDeviceDescriptor() {
        return getVendorDescriptor().findDevice(getDeviceID());
    }

    /**
     * Gets the status of this unit
     */
    public final int getStatus() {
        return device.readConfigWord(PCI_STATUS);
    }

    /**
     * Gets the command info of this unit
     */
    public final int getCommand() {
        return device.readConfigWord(PCI_COMMAND);
    }

    /**
     * Gets the command info of this unit
     */
    public final void setCommand(int command) {
        device.writeConfigWord(PCI_COMMAND, command);
    }

    /**
     * Gets the major class of this unit
     */
    public final int getBaseClass() {
        return majorClass;
    }

    /**
     * Gets the subclass of this unit
     */
    public final int getSubClass() {
        return subClass;
    }

    /**
     * Gets the minor class (API identification) of this unit
     */
    public final int getMinorClass() {
        return minorClass;
    }

    /**
     * Sets the minor class (API identification) of this unit
     *
     * @param v
     */
    public final void setMinorClass(int v) {
        device.writeConfigByte(PCI_CLASS_PROG, v);
        minorClass = device.readConfigByte(PCI_CLASS_PROG);
    }

    /**
     * Gets the revision of this unit
     */
    public final int getRevision() {
        return revision;
    }

    public int getBist() {
        return device.readConfigByte(PCI_BIST);
    }

    public final int getHeaderType() {
        return headerTypeRaw & 0x7F;
    }

    /**
     * Is this unit a multifunctional device?
     */
    public final boolean isMultiFunctional() {
        return ((headerTypeRaw & 0x80) != 0);
    }

    public int getLatency() {
        return device.readConfigByte(PCI_LATENCY_TIMER);
    }

    public int getCacheLineSize() {
        return device.readConfigByte(PCI_CACHE_LINE_SIZE);
    }

    /**
     * Gets a capability with a given id.
     *
     * @return Null if not found, otherwise the capability with the given id.
     */
    public final Capability getCapability(int id) {
        if (capabilities == null) {
            capabilities = Collections.unmodifiableMap(readCapabilities());
        }
        return capabilities.get(id);
    }

    /**
     * Gets all capabilities.
     *
     * @return
     */
    public final Collection<Capability> getCapabilities() {
        if (capabilities == null) {
            capabilities = Collections.unmodifiableMap(readCapabilities());
        }
        return capabilities.values();
    }

    /**
     * Is this a header type 0 configuration.
     *
     * @return
     */
    public final boolean isHeaderType0() {
        return (getHeaderType() == HEADER_TYPE_NORMAL);
    }

    /**
     * Is this a header type 1 configuration.
     *
     * @return
     */
    public final boolean isHeaderType1() {
        return (getHeaderType() == HEADER_TYPE_BRIDGE);
    }

    /**
     * Is this a header type 2 configuration.
     *
     * @return
     */
    public final boolean isHeaderType2() {
        return (getHeaderType() == HEADER_TYPE_CARDBUS);
    }

    /**
     * Gets this configuration as a header type 0 (normal devices) accessor.
     *
     * @return
     * @throws ClassCastException If header type != 0.
     */
    public final PCIHeaderType0 asHeaderType0() {
        return (PCIHeaderType0) this;
    }

    /**
     * Gets this configuration as a header type 1 (pci-pci bridge) accessor.
     *
     * @return
     * @throws ClassCastException If header type != 1.
     */
    public final PCIHeaderType1 asHeaderType1() {
        return (PCIHeaderType1) this;
    }

    /**
     * Gets this configuration as a header type 2 (cardbus bridge) accessor.
     *
     * @return
     * @throws ClassCastException If header type != 2.
     */
    public final PCIHeaderType2 asHeaderType2() {
        return (PCIHeaderType2) this;
    }

    /**
     * Convert myself to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("device=0x");
        sb.append(NumberUtils.hex(getDeviceID(), 4));

        sb.append(", vendor=0x");
        sb.append(NumberUtils.hex(getVendorID(), 4));
        sb.append(", ");

        sb.append(", class=");
        sb.append(NumberUtils.hex(getBaseClass(), 2));
        sb.append(':');
        sb.append(NumberUtils.hex(getSubClass(), 2));
        sb.append(':');
        sb.append(NumberUtils.hex(getMinorClass(), 2));

        sb.append(", revision=");
        sb.append(getRevision());

        sb.append(", headertype=");
        sb.append(getHeaderType());

        final Collection<Capability> caps = getCapabilities();
        if (!caps.isEmpty()) {
            sb.append(", capabilities [");
            boolean first = true;
            for (Capability c : caps) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(c);
            }
            sb.append(']');
        }

        return sb.toString();
    }

    /**
     * Gets a 32-bit int from the device's configuration space.
     *
     * @param offset Byte offset of the requested dword.
     * @return
     */
    public int getDWord(int offset) {
        return device.readConfigDword(offset);
    }

    /**
     * Read the capability list.
     *
     * @return
     */
    private Map<Integer, Capability> readCapabilities() {
        if ((getStatus() & PCI_STATUS_CAP_LIST) == 0) {
            return Collections.emptyMap();
        }
        int ptr = device.readConfigByte(PCI_CAPABILITY_LIST) & ~0x03;
        if (ptr == 0) {
            return Collections.emptyMap();
        }
        final TreeMap<Integer, Capability> map = new TreeMap<Integer, Capability>();
        while (ptr != 0) {
            final Capability c = Capability.createCapability(device, ptr);
            map.put(c.getId(), c);
            ptr = device.readConfigByte(ptr + 1) & ~0x03;
        }
        return map;
    }
}
