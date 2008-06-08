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

package org.jnode.driver.net.lance;

import org.apache.log4j.Logger;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;

/**
 * @author Chris Cole
 */
public class InitializationBlock32Bit {
    public static final int INIT_BLOCK_SIZE = 0x1C;

    private MemoryResource mem;
    private int offset;

    public InitializationBlock32Bit(MemoryResource mem, int offset, short mode,
            EthernetAddress physicalAddr, long logicalAddr, RxDescriptorRing rxRing,
            TxDescriptorRing txRing) {

        this.mem = mem;
        this.offset = offset;

        // Populate the initial data structure
        mem.setShort(offset + 0x00, mode);
        mem.setByte(offset + 0x02, getEncodedRingLength(rxRing.getLength()));
        mem.setByte(offset + 0x03, getEncodedRingLength(txRing.getLength()));
        mem.setByte(offset + 0x04, physicalAddr.get(0));
        mem.setByte(offset + 0x05, physicalAddr.get(1));
        mem.setByte(offset + 0x06, physicalAddr.get(2));
        mem.setByte(offset + 0x07, physicalAddr.get(3));
        mem.setByte(offset + 0x08, physicalAddr.get(4));
        mem.setByte(offset + 0x09, physicalAddr.get(5));
        mem.setInt(offset + 0x0C, (int) (logicalAddr & 0xFFFFFFFF));
        mem.setInt(offset + 0x10, (int) ((logicalAddr >> 32) & 0xFFFFFFFF));
        mem.setInt(offset + 0x14, rxRing.getAddressAs32());
        mem.setInt(offset + 0x18, txRing.getAddressAs32());
    }

    private byte getEncodedRingLength(int ringLength) {
        byte encoded = 0;
        while (ringLength != 1) {
            ringLength = ringLength >> 1;
            encoded += 1;
        }
        return (byte) (encoded << 4);
    }

    public void dumpData(Logger out) {
        out.debug("Intialization Block - 32 bit mode");
        for (int i = 0; i <= INIT_BLOCK_SIZE - 1; i += 4) {
            out.debug(
                    "0x" + NumberUtils.hex(mem.getAddress().toInt() + offset + i) + 
                    " : 0x" + NumberUtils.hex((byte) i) + 
                    " : 0x" + NumberUtils.hex(mem.getInt(offset + i)));
        }
    }
}
