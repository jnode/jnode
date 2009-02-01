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
 
package org.jnode.driver.bus.smbus;

import org.jnode.driver.Device;

/**
 * SMBus device extension.
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

public class SMBusDevice extends Device {

    int address;

    /**
     * 8 bits value bits assignment 7-6 AddressType 5-1 reserved 0 PEC supported
     */
    int capabilities; // 8 bits
    public static final int ADDRESS_FIXED_ADDRESS = 0; // bit [7-6] 00 Fixed Address devices are
    // identified first.
    public static final int ADDRESS_DYNAMIC_PERSISTENT = 1; // bit [7-6] 01 Dynamic and Persistent
    // Address devices are identified next.
    public static final int ADDRESS_DYNAMIC_VOLATILE = 2; // bit [7-6] 10 Dynamic and Volatile
    // Address devices are identified next.
    public static final int ADDRESS_RANDOM = 3; // bit [7-6] 11 Dynamic and Volatile Address
    // devices are identified next.

    /**
     * 8 bits bits assignment 7-6 reserved 5-3 version (001 for SMBus 2.0) 2-0 silicon version
     */
    int version; // 8 bits
    public static final int VERSION_MASK = 0x38;
    public static final int VERSION_V10 = 0;
    public static final int VERSION_V11 = 0;
    public static final int VERSION_V20 = 1;

    int vendorid; // 16 bits
    int deviceid; // 16 bits

    /**
     * 16 bits bits assignment 15-4 supported protocols bit 15 Reserved for future definition under
     * the SMBus specifications. bit 14 Reserved for future definition under the SMBus
     * specifications. bit 13 Reserved for future definition under the SMBus specifications. bit 12
     * Reserved for future definition under the SMBus specifications. bit 11 Reserved for future
     * definition under the SMBus specifications. bit 10 Reserved for future definition under the
     * SMBus specifications. bit 9 Reserved for future definition under the SMBus specifications.
     * bit 8 Reserved for future definition under the SMBus specifications. bit 7 Reserved for
     * future definition under the SMBus specifications. bit 6 IPMI Device supports additional
     * interface access and capabilities per IPMI specifications bit 5 ASF Device supports
     * additional interface access and capabilities per ASF specifications bit 4 OEM Device
     * supports vendor-specific access and capabilities per the Subsystem Vendor ID and Subsystem
     * Device ID fields returned by discoverable SMBus devices. The Subsystem Vendor ID identifies
     * the vendor or defining body that has specified the behavior of the device. The Subsystem
     * Device ID is used in conjunction with the System Vendor ID to specify a particular level of
     * functional equivalence for the device. 3-0 SMBus version
     */
    int interfaceid; // 16 bits
    public static final int VERSION_1_0 = 0; // 0000 do not use in ARP tables
    public static final int VERSION_1_1 = 1; // 0001 do not use in ARP tables
    public static final int VERSION_2_0 = 4; // 0100 (0010 and 0011 are reserved)

    int subsystemvendorid; // 16 bits
    int subsystemdeviceid; // 16 bits
    int vendorspecificid; // 16 bits

    public SMBusDevice(SMBus bus, String name, int version, int vendorid, int deviceid, int interfaceid,
                       int subsystemvendorid, int subsystemdeviceid, int vendorspecificid) {
        super(bus, name);
        this.version = version;
        this.vendorid = vendorid;
        this.deviceid = deviceid;
        this.interfaceid = interfaceid;
        this.subsystemvendorid = subsystemvendorid;
        this.subsystemdeviceid = subsystemdeviceid;
        this.vendorspecificid = vendorspecificid;
    }

    public SMBusDevice(SMBus bus, String name, int version) {
        this(bus, name, version, 0, 0, 0, 0, 0, 0);
    }

    public int getAddressType() {
        if (getVersion() < VERSION_V20)
            return -1;
        return capabilities & 0xc0 >> 6;
    }

    public boolean isPECSupported() {
        if (getVersion() < VERSION_V20)
            return false;
        return (capabilities & 0x01) > 0;
    }

    public int getVersion() {
        int version = ((this.version & VERSION_MASK) >> 3) & 0xff;
        return version;
    }

    public int getSiliconVersion() {
        if (getVersion() < VERSION_V20)
            return -1;
        return version & 0x7;
    }

    public boolean isIPMIInterface() {
        if (getVersion() < VERSION_V20)
            return false;
        return (interfaceid & 0x40) > 0;
    }

    public boolean isASFInterface() {
        if (getVersion() < VERSION_V20)
            return false;
        return (interfaceid & 0x20) > 0;
    }

    public boolean isOEMInterface() {
        if (getVersion() < VERSION_V20)
            return false;
        return (interfaceid & 0x10) > 0;
    }

}
