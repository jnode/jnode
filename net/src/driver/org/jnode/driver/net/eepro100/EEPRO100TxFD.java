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
 
package org.jnode.driver.net.eepro100;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;

/**
 * @author flesire
 */
public class EEPRO100TxFD {
    private int TxFDSize = 16;
    private int DataBufferSize = 1536;
    
    /**
     * The actual data
     */
    private final byte[] data;
    
    /**
     * MemoryResource mapper around data
     */
    private final MemoryResource mem;
    
    /**
     * Offset within mem of first DPD
     */
    private final int firstDPDOffset;
    
    /**
     * 32-bit address first DPD
     */
    private final Address firstDPDAddress;
    
    private int bufferAddress;

    
    public EEPRO100TxFD(ResourceManager rm) {
        // Create a large enough buffer
        final int size = (TxFDSize + DataBufferSize) + 16 /* alignment */;
        this.data = new byte[size];
        this.mem = rm.asMemoryResource(data);

        final Address memAddr = mem.getAddress();
        // int addr = Address.as32bit(memAddr);
        int offset = 0;

        this.firstDPDOffset = offset;
        this.firstDPDAddress = memAddr.add(firstDPDOffset);
    }

    /**
     * Initialize this ring to its default (empty) state
     */
    public void initialize(SocketBuffer src) throws IllegalArgumentException {
        // Setup the DPD

        // Copy the data from the buffer
        final int len = src.getSize();
        if (len > EthernetConstants.ETH_FRAME_LEN) {
            throw new IllegalArgumentException(
                "Length must be <= ETH_FRAME_LEN");
        }
        src.get(data, firstDPDOffset, 0, len);
    }

    /**
     * Gets the address of the first DPD in this buffer.
     */
    public Address getFirstDPDAddress() {
        return firstDPDAddress;
    }

    final int getStatus() {
        return mem.getInt(0);
    }

    final void setStatus(int value) {
        mem.setInt(0, value);
    }

    final int getCommand() {
        return mem.getInt(2);
    }

    final void setCommand(int value) {
        mem.setInt(2, value);
    }

    final int getLink() {
        return mem.getInt(4);
    }

    final void setLink(int value) {
        mem.setInt(4, value);
    }

    final void setDescriptorAddress(int value) {
        mem.setInt(8, value);
    }

    final int getCount() {
        return mem.getInt(12);
    }

    final void setCount(int value) {
        mem.setInt(12, value);
    }

    int getBufferAddress() {
        return this.bufferAddress;
    }

    void bufferAddress0(int address) {
        mem.setInt(16, address);
    }

    void bufferSize0(int size) {
        mem.setInt(20, size);
    }

    //  put paramater array into the cmd buffer
    void setParams(byte[] p) {
        for (int i = 0; i < p.length; i++) {
            mem.setShort(i + 8, p[i]);
        }
    }

    public int getFirstDPDOffset() {
        return firstDPDOffset;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("Status  : ");
        str.append(NumberUtils.hex(this.getStatus()));
        str.append("\n");
        str.append("Command : ");
        str.append(NumberUtils.hex(this.getCommand()));
        str.append("\n");
        str.append("Link    : ");
        str.append(NumberUtils.hex(this.getLink()));
        str.append("\n");
        str.append("Count   : ");
        str.append(NumberUtils.hex(this.getCount()));
        str.append("\n");
        return str.toString();
    }
}
