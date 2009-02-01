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
 
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPTimestampHeader extends ICMPExHeader {

    private final int originateTimestamp;
    private final int receiveTimestamp;
    private final int transmitTimestamp;

    /**
     * @param type
     * @param identifier
     * @param seqNumber
     * @param originateTimestamp
     * @param receiveTimestamp
     * @param transmitTimestamp
     */
    public ICMPTimestampHeader(int type, int identifier, int seqNumber, int originateTimestamp,
            int receiveTimestamp, int transmitTimestamp) {
        super(type, 0, identifier, seqNumber);
        if ((type != ICMP_TIMESTAMP) && (type != ICMP_TIMESTAMPREPLY)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
        this.originateTimestamp = originateTimestamp;
        this.receiveTimestamp = receiveTimestamp;
        this.transmitTimestamp = transmitTimestamp;
    }

    /**
     * @param skbuf
     */
    public ICMPTimestampHeader(SocketBuffer skbuf) {
        super(skbuf);
        final int type = getType();
        if ((type != ICMP_TIMESTAMP) && (type != ICMP_TIMESTAMPREPLY)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
        this.originateTimestamp = skbuf.get32(8);
        this.receiveTimestamp = skbuf.get32(12);
        this.transmitTimestamp = skbuf.get32(16);
    }

    /**
     * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
     */
    protected void doPrefixTo(SocketBuffer skbuf) {
        super.doPrefixTo(skbuf);
        skbuf.set32(8, originateTimestamp);
        skbuf.set32(12, receiveTimestamp);
        skbuf.set32(16, transmitTimestamp);
    }

    /**
     * @see org.jnode.net.LayerHeader#getLength()
     */
    public int getLength() {
        return 20;
    }

    /**
     * Gets the originate timestamp
     */
    public int getOriginateTimestamp() {
        return originateTimestamp;
    }

    /**
     * Gets the receive timestamp
     */
    public int getReceiveTimestamp() {
        return receiveTimestamp;
    }

    /**
     * Gets the transmit timestamp
     */
    public int getTransmitTimestamp() {
        return transmitTimestamp;
    }
}
