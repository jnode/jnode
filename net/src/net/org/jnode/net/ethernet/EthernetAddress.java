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
 
package org.jnode.net.ethernet;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.util.NumberUtils;

/**
 * Wrapper for an 6-byte ethernet address. Also called MAC address.
 * 
 * @author epr
 */
public class EthernetAddress implements HardwareAddress, Serializable {

    private static final long serialVersionUID = -4125730887819599814L;

    private final byte[] address;
    private static final int length = EthernetConstants.ETH_ALEN;
    public static final EthernetAddress BROADCAST = new EthernetAddress("FF-FF-FF-FF-FF-FF");

    /**
     * Create a new instance
     * 
     * @param address
     * @param offset
     */
    public EthernetAddress(byte[] address, int offset) throws IllegalArgumentException {
        if (offset + length > address.length) {
            throw new IllegalArgumentException("Invalid address length");
        }
        this.address = new byte[length];
        System.arraycopy(address, offset, this.address, 0, length);
    }

    /**
     * Create a new instance
     * 
     * @param skbuf
     * @param offset
     */
    public EthernetAddress(SocketBuffer skbuf, int offset) throws IllegalArgumentException {
        this.address = new byte[length];
        skbuf.get(address, 0, offset, length);
    }

    /**
     * Create a new instance
     * 
     * @param addressStr
     */
    public EthernetAddress(String addressStr) throws IllegalArgumentException {
        final StringTokenizer tok = new StringTokenizer(addressStr, "-:");
        if (tok.countTokens() != length) {
            throw new IllegalArgumentException("Invalid address " + addressStr);
        }
        address = new byte[length];
        for (int i = 0; i < length; i++) {
            try {
                address[i] = (byte) Integer.parseInt(tok.nextToken(), 16);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not an ethernet address " + addressStr);
            }
        }
    }

    /**
     * Create a new instance
     * 
     * @param b0
     * @param b1
     * @param b2
     * @param b3
     * @param b5
     */
    public EthernetAddress(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5) {
        address = new byte[length];
        address[0] = b0;
        address[1] = b1;
        address[2] = b2;
        address[3] = b3;
        address[4] = b4;
        address[5] = b5;
    }

    /**
     * Is this address equal to the given address.
     * 
     * @param o
     */
    public boolean equals(EthernetAddress o) {
        for (int i = 0; i < length; i++) {
            if (this.address[i] != o.address[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is this address equal to the given address.
     * 
     * @param o
     */
    public boolean equals(HardwareAddress o) {
        if (o instanceof EthernetAddress) {
            return equals((EthernetAddress) o);
        } else {
            return false;
        }
    }

    /**
     * Is this address equal to the given address.
     * 
     * @param o
     */
    public boolean equals(Object o) {
        if (o instanceof EthernetAddress) {
            return equals((EthernetAddress) o);
        } else {
            return false;
        }
    }

    /**
     * Gets the length of this address in bytes
     */
    public final int getLength() {
        return length;
    }

    /**
     * Gets the address-byte at a given index
     * 
     * @param index
     */
    public final byte get(int index) {
        return address[index];
    }

    /**
     * Write this address to a given offset in the given buffer
     * 
     * @param skbuf
     * @param skbufOffset
     */
    public void writeTo(SocketBuffer skbuf, int skbufOffset) {
        skbuf.set(skbufOffset, address, 0, length);
    }

    /**
     * Write this address to a given offset in the given buffer
     * 
     * @param dst
     * @param dstOffset
     */
    public void writeTo(byte[] dst, int dstOffset) {
        System.arraycopy(address, 0, dst, dstOffset, length);
    }

    /**
     * Is this a broadcast address?
     */
    public boolean isBroadcast() {
        for (int i = 0; i < length; i++) {
            if ((0xFF & address[i]) != 0xFF) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the default broadcast address for this kind of hardware address.
     */
    public HardwareAddress getDefaultBroadcastAddress() {
        return BROADCAST;
    }

    /**
     * Gets the type of this address. This type is used by (e.g.) ARP.
     */
    public int getType() {
        return 1; // For ethernet
    }

    /**
     * Convert this address to a string representation
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder(2 + 3 * (length - 1));
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                b.append(':');
            }
            b.append(NumberUtils.hex(address[i] & 0xFF, 2));
        }
        return b.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int v = 0;
        for (int i = 0; i < length; i++) {
            v ^= (address[i] & 0xFF) << (i * 3);
        }
        return v;
    }
}
