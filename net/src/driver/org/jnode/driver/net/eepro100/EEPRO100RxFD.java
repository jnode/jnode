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

package org.jnode.driver.net.eepro100;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.vmmagic.unboxed.Address;

/**
 * This class provide access to Receive Descriptor.
 *
 * @author flesire
 */
public class EEPRO100RxFD {

    private int RxFDSize = 16;
    private int DataBufferSize = 1518;
    private static final int FRAME_SIZE = EthernetConstants.ETH_FRAME_LEN;

    private int bufferAddress;
    private byte[] data;
    private MemoryResource mem;

    /**
     * Offset within mem of first UDP
     */
    private final int firstUPDOffset;
    /**
     * Offset within mem of first ethernet frame
     */
    private final int firstFrameOffset;
    /**
     * 32-bit address first UDP
     */
    private final Address firstUPDAddress;
    /**
     * 32-bit address of first ethernet frame
     */
    private final Address firstFrameAddress;

    private int nrFrames = 1;

    /**
     *
     */
    public EEPRO100RxFD(ResourceManager rm) {
        final int size = (nrFrames * (RxFDSize + FRAME_SIZE)) + 16;
        this.data = new byte[size];
        this.mem = rm.asMemoryResource(data);

        final Address memAddr = mem.getAddress();
        int addr = memAddr.toInt();
        int offset = 0;
        // Align on 16-byte boundary
        while ((addr & 15) != 0) {
            addr++;
            offset++;
        }

        this.firstUPDOffset = offset;
        this.firstUPDAddress = memAddr.add(firstUPDOffset);
        this.firstFrameOffset = firstUPDOffset + (nrFrames * RxFDSize);
        this.firstFrameAddress = memAddr.add(firstFrameOffset);

        this.bufferAddress = mem.getAddress().toInt();
        setRxBufferAddress(0xffffffff);
    }

    public void initialize() {
        // Setup each UPD
        for (int i = 0; i < nrFrames; i++) {
            final int updOffset = firstUPDOffset + (i * RxFDSize);
            // Set next UPD ptr
            if (i + 1 < nrFrames) {
                mem.setInt(updOffset + 0, firstUPDAddress.toInt() + ((i + 1) * RxFDSize));
            } else {
                mem.setInt(updOffset + 0, firstUPDAddress.toInt());
            }
            // Set pkt status
            mem.setInt(updOffset + 4, 0);
            // Set fragment address
            mem.setInt(updOffset + 8, firstFrameAddress.toInt() + (i * FRAME_SIZE));
            // Set fragment size
            mem.setInt(updOffset + 12, FRAME_SIZE | (1 << 31));
        }
    }

    public final int getStatus() {
        return mem.getInt(0);
    }

    public final void setStatus(int value) {
        mem.setInt(0, value);
    }

    public final int getCommand() {
        return mem.getInt(2);
    }

    public final void setCommand(int cmd) {
        mem.setInt(2, cmd);
    }

    public final void setLink(int address) {
        mem.setInt(4, address);
    }

    public final void setRxBufferAddress(int address) {
        mem.setInt(8, address);
    }

    public final int getCount() {
        return mem.getInt(12);
    }

    public final void setCount(int size) {
        mem.setInt(12, size);
    }

    public final int getSize() {
        return mem.getInt(14);
    }

    public final void setSize(int size) {
        mem.setInt(14, size);
    }

    public final void flushHeader() {
        //SPROCKET_flush64(frameAddress);
        //SPROCKET_drainWriteBuffer();
    }

    public final void cleanHeader() {
        /*
         * SPROCKET_clean64(frameAddress); SPROCKET_drainWriteBuffer();
         */
    }


    public final String print() {
        return (Integer.toHexString(bufferAddress) + ": "
            + Integer.toHexString(mem.getInt(4)) + ' '
            + Integer.toHexString(mem.getInt(8)) + ' '
            + Integer.toHexString(mem.getInt(12)));
    }

    /**
     * @return Returns the bufferAddress.
     */
    public int getBufferAddress() {
        return bufferAddress;
    }

    /**
     * @return
     */
    public byte[] getDataBuffer() {
        byte[] buf = new byte[DataBufferSize];
        mem.getBytes(bufferAddress, buf, 0, buf.length);
        return buf;
    }

    /**
     * @return
     */
    public SocketBuffer getPacket() {
        int pktLen = this.getCount() & 0x3fff;
        final SocketBuffer skbuf = new SocketBuffer();
        skbuf.append(data, 0, pktLen);
		return skbuf; 
	}
}
