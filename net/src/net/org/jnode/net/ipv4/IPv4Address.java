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
 
package org.jnode.net.ipv4;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;

/**
 * @author epr
 */
public class IPv4Address implements ProtocolAddress, Serializable {

    private static final long serialVersionUID = 4663183102157418943L;

    private static final int length = 4;
    private final byte[] address;
    private InetAddress inetAddress;

    public static final IPv4Address ANY = new IPv4Address("0.0.0.0");
    public static final IPv4Address BROADCAST = new IPv4Address("255.255.255.255");

    private static final IPv4Address DEFAULT_ANY_SUBNETMASK = new IPv4Address("0.0.0.0");
    private static final IPv4Address DEFAULT_CLASS_A_SUBNETMASK = new IPv4Address("255.0.0.0");
    private static final IPv4Address DEFAULT_CLASS_B_SUBNETMASK = new IPv4Address("255.255.0.0");
    private static final IPv4Address DEFAULT_CLASS_C_SUBNETMASK = new IPv4Address("255.255.255.0");
    private static final IPv4Address DEFAULT_CLASS_D_SUBNETMASK = new IPv4Address("255.255.255.0");

    /** Useful Inet4Address broadcast address constant */
    public static final Inet4Address BROADCAST_ADDRESS = (Inet4Address) BROADCAST.toInetAddress();

    /**
     * Reads an address at a given offset from the given buffer
     * @param skbuf
     * @param offset
     */
    public static Inet4Address readFrom(SocketBuffer skbuf, int offset) {
        byte[] address = new byte[length];
        skbuf.get(address, 0, offset, length);
        try {
            return (Inet4Address) InetAddress.getByAddress(address);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Writes an address to a given offset in the given buffer
     * @param skbuf
     * @param skbufOffset
     * @param address
     */
    public static void writeTo(SocketBuffer skbuf, int skbufOffset, Inet4Address address) {
        skbuf.set(skbufOffset, address.getAddress(), 0, length);
    }

    /**
     * Create a new instance
     * @param src
     * @param offset
     */
    public IPv4Address(byte[] src, int offset) {
        address = new byte[length];
        System.arraycopy(src, offset, address, 0, length);
    }

    /**
     * Create a new instance
     * @param address The array that is directly used. Not copied!
     */
    private IPv4Address(byte[] address) {
        this.address = address;
    }

    /**
     * Create a new instance
     * @param skbuf
     * @param offset
     */
    public IPv4Address(SocketBuffer skbuf, int offset) {
        address = new byte[length];
        skbuf.get(address, 0, offset, length);
    }

    /**
     * Create a new instance from a String representation
     * @param addrStr
     * @throws IllegalArgumentException
     */
    public IPv4Address(String addrStr) throws IllegalArgumentException {
        final StringTokenizer tok = new StringTokenizer(addrStr, ".");
        if (tok.countTokens() != length) {
            throw new IllegalArgumentException("Not an IPv4 address " + addrStr);
        }
        address = new byte[length];
        for (int i = 0; i < length; i++) {
            try {
                address[i] = (byte) Integer.parseInt(tok.nextToken());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not a valid IPv4 address " + addrStr);
            }
        }
    }

    /**
     * Create a new instance from a java.net.InetAddress
     * @param inetAddress
     */
    public IPv4Address(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        this.address = inetAddress.getAddress();
        if (address.length != length) {
            throw new IllegalArgumentException("inetAddress.length is incorrect");
        }
    }

    /**
     * Is this address equal to the given address?
     */
    public final boolean equals(IPv4Address other) {
        if (other == null) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (address[i] != other.address[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is this address equal to the given address.
     * @param o
     */
    public boolean equals(ProtocolAddress o) {
        if (o instanceof IPv4Address) {
            return equals((IPv4Address) o);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(Object obj) {
        if (obj instanceof IPv4Address) {
            return equals((IPv4Address) obj);
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
     * @param index
     */
    public final byte get(int index) {
        return address[index];
    }

    /**
     * Gets an array with the bytes of this address
     */
    public final byte[] getBytes() {
        byte[] b = new byte[4];
        System.arraycopy(address, 0, b, 0, 4);
        return b;
    }

    /**
     * Write this address to a given offset in the given buffer
     * @param skbuf
     * @param skbufOffset
     */
    public final void writeTo(SocketBuffer skbuf, int skbufOffset) {
        skbuf.set(skbufOffset, address, 0, length);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "" + (address[0] & 0xFF) + '.' + (address[1] & 0xFF) + '.' + (address[2] & 0xFF) +
                '.' + (address[3] & 0xFF);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int v0 = address[0] & 0xFF;
        final int v1 = address[1] & 0xFF;
        final int v2 = address[2] & 0xFF;
        final int v3 = address[3] & 0xFF;
        return (v3 << 24) | (v2 << 16) | (v1 << 8) | v0;
    }

    /** 
     * Is this a class A address.
     * Class A = 0.0.0.0 - 127.255.255.255
     */
    public boolean isClassA() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 0) && (b0 <= 127);
    }

    /** 
     * Is this a class B address.
     * Class B = 128.0.0.0 - 191.255.255.255
     */
    public boolean isClassB() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 128) && (b0 <= 191);
    }

    /** 
     * Is this a class C address.
     * Class C = 192.0.0.0 - 223.255.255.255
     */
    public boolean isClassC() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 192) && (b0 <= 223);
    }

    /** 
     * Is this a class D (multicast) address.
     * Class D = 224.0.0.0 - 239.255.255.255
     */
    public boolean isClassD() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 224) && (b0 <= 239);
    }

    /** 
     * Is this a class E (experimental) address.
     * Class E = 240.0.0.0 - 247.255.255.255
     */
    public boolean isClassE() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 240) && (b0 <= 247);
    }

    /** 
     * Is this an unicast address.
     * Unicast = 0.0.0.0 - 223.255.255.255
     */
    public boolean isUnicast() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 0) && (b0 <= 223);
    }

    /** 
     * Is this a multicast address.
     * Multicast = 224.0.0.0 - 239.255.255.255
     */
    public boolean isMulticast() {
        final int b0 = address[0] & 0xFF;
        return (b0 >= 224) && (b0 <= 239);
    }

    /** 
     * Is this a broadcast address.
     * Broadcast = hostID = -1
     */
    public boolean isBroadcast() {
        final int b3 = address[3] & 0xFF;
        return (b3 == 0xFF);
    }

    /**
     * Is this a host address.
     * A host address has <code>hostID != 0</code>
     */
    public boolean isHost() {
        final int b3 = address[3] & 0xFF;
        return (b3 != 0);
    }

    /**
     * Is this an Any address.
     * An Any address is equal to "0.0.0.0"
     */
    public boolean isAny() {
        for (int i = 0; i < length; i++) {
            if (address[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is this a network address.
     * A network address has <code>hostID == 0</code>
     */
    public boolean isNetwork() {
        final int b3 = address[3] & 0xFF;
        return (b3 == 0);
    }

    /**
     * Does this address matches a given mask?
     * @param mask
     */
    public boolean matches(IPv4Address otherAddress, IPv4Address mask) {
        for (int i = 0; i < length; i++) {
            final int a = address[i] & 0xFF;
            final int o = otherAddress.address[i] & 0xFF;
            final int m = mask.address[i] & 0xFF;
            if ((a & m) != (o & m)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate the and or this address with the given mask.
     * @param mask
     */
    public IPv4Address and(IPv4Address mask) {
        final byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = (byte) (address[i] & mask.address[i]);
        }
        return new IPv4Address(res);
    }

    /**
     * Convert to a java.net.InetAddress
     * @see java.net.InetAddress
     * @see java.net.Inet4Address
     * @return This address as java.net.InetAddress
     */
    public InetAddress toInetAddress() {
        if (inetAddress == null) {
            try {
                inetAddress = InetAddress.getByAddress(address);
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }
        return inetAddress;
    }

    /**
     * Convert to a byte array.
     * @see org.jnode.net.ProtocolAddress#toByteArray()
     */
    public byte[] toByteArray() {
        final byte[] result = new byte[address.length];
        System.arraycopy(address, 0, result, 0, address.length);
        return result;
    }

    /**
     * Gets the default subnet mask for this address 
     */
    public IPv4Address getDefaultSubnetmask() {
        if (isAny()) {
            return DEFAULT_ANY_SUBNETMASK;
        } else if (isClassA()) {
            return DEFAULT_CLASS_A_SUBNETMASK;
        } else if (isClassB()) {
            return DEFAULT_CLASS_B_SUBNETMASK;
        } else if (isClassC()) {
            return DEFAULT_CLASS_C_SUBNETMASK;
        } else if (isClassD()) {
            return DEFAULT_CLASS_D_SUBNETMASK;
        } else {
            throw new IllegalArgumentException("Unknown address class");
        }
    }

    /**
     * Gets the type of this address.
     * This type is used by (e.g.) ARP.
     */
    public int getType() {
        return EthernetConstants.ETH_P_IP;
    }
}
