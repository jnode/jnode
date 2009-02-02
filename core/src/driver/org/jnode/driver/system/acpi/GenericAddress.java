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
 
package org.jnode.driver.system.acpi;

/**
 * GenericAddress.
 * <p/>
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Licence: GNU LGPL</p>
 * <p> </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class GenericAddress {
    byte addressSpace;
    byte width;
    byte offset;
    long address;

    public static final byte SYSTEM_MEMORY = 0;
    public static final byte SYSTEM_IO = 1;
    public static final byte PCI = 2;
    public static final byte EMBEDED_CONTROLER = 3;
    public static final byte SMBUS = 4;
    public static final byte FIXED_HARDWARE = (byte) 0x7ff;

    public GenericAddress(byte[] data) {
        addressSpace = data[0];
        width = data[1];
        offset = data[2];
        // byte 3 is reserved and should be 0
        address = 0;
        for (int i = 4; i < 12; i++) {
            address <<= 8;
            address += data[i] & 0xff;
        }
    }

    public String toString() {
        String space;
        String add;
        switch (addressSpace) {
            case SYSTEM_MEMORY:
                space = "Memory";
                add = Long.toHexString(address);
                break;
            case SYSTEM_IO:
                space = "IO";
                add = Long.toHexString(address);
                break;
            case PCI:
                space = "PCI";
                int device = (int) ((address & 0xff00000000L) >> 24);
                int offset = (int) ((address & 0xffffL));
                int function = (int) ((address & 0xffff0000L) >> 16);
                add = "Bus(0)Device(" + device + ")Function(" + function + ")Offset(" + offset + ")";
                break;
            case EMBEDED_CONTROLER:
                space = "Controler";
                add = Long.toHexString(address);
                break;
            case SMBUS:
                space = "SMBus";
                add = Long.toHexString(address);
                break;
            case FIXED_HARDWARE:
                space = "Hardware";
                add = Long.toHexString(address);
                break;
            default:
                space = "addressSpace-" + Integer.toHexString(addressSpace & 0xff);
                add = Long.toHexString(address);
                break;
        }

        return "<" + space + ":" + add + "/" + width + "," + offset + ">";
    }
}
