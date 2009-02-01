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
 
package org.jnode.net.ipv4.bootp;

import java.net.DatagramPacket;

import org.jnode.net.SocketBuffer;

/**
 * RFC 1542.
 * 
 * @author markhale
 */
public class BOOTPMessage {

    /** Size of the BOOTP vendor-specific area (64 bytes) */
    public static final int OPTIONS_SIZE = 64;
    
    /** Size of the BOOTP message (300 bytes) */
    public static final int SIZE = BOOTPHeader.SIZE + OPTIONS_SIZE;

    private final BOOTPHeader header;

    /**
     * Create a new message
     */
    public BOOTPMessage(BOOTPHeader hdr) {
        header = hdr;
    }

    public BOOTPMessage(SocketBuffer skbuf) {
        this(new BOOTPHeader(skbuf));
    }

    public BOOTPMessage(DatagramPacket packet) {
        this(new BOOTPHeader(
                new SocketBuffer(packet.getData(), packet.getOffset(), packet.getLength())));
    }

    public BOOTPHeader getHeader() {
        return header;
    }

    /**
     * Gets this message as a DatagramPacket
     */
    public DatagramPacket toDatagramPacket() {
        final SocketBuffer skbuf = new SocketBuffer();
        skbuf.insert(OPTIONS_SIZE);

        header.prefixTo(skbuf);
        final byte[] data = skbuf.toByteArray();
        return new DatagramPacket(data, data.length);
    }
}
