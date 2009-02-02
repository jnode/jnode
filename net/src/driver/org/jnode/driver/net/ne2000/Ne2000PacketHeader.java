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
 
package org.jnode.driver.net.ne2000;

import org.jnode.util.NumberUtils;

/**
 * Represents a packet header of a Ne2000 PCI ringbuffer
 * 
 * @author epr
 */
public class Ne2000PacketHeader {

    private final int status;
    private final int nextPacketPage;
    private final int length;

    /**
     * Create a new instance
     * 
     * @param hdr
     * @param hdrOffset
     */
    public Ne2000PacketHeader(byte[] hdr, int hdrOffset) {
        status = hdr[hdrOffset + 0] & 0xFF;
        nextPacketPage = hdr[hdrOffset + 1] & 0xFF;
        length = ((hdr[hdrOffset + 3] & 0xFF) << 8) | (hdr[hdrOffset + 2] & 0xFF);
    }

    /**
     * Gets the length of this packet
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the page on which the next packet is or will be located,
     */
    public int getNextPacketPage() {
        return nextPacketPage;
    }

    /**
     * Gets the status of this packet
     */
    public int getStatus() {
        return status;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "state:0x" + NumberUtils.hex(status, 2) + ", " + "next:0x" +
                NumberUtils.hex(nextPacketPage, 2) + ", " + "length:0x" +
                NumberUtils.hex(length, 4);
    }

}
