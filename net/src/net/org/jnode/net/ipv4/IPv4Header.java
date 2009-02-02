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
 
package org.jnode.net.ipv4;

import org.jnode.net.NetworkLayerHeader;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class IPv4Header implements NetworkLayerHeader, IPv4Constants {

    /** IP version */
    private int version;
    
    /** Length of header in bytes */
    private int hdrlength;
    
    /** Type of service */
    private int tos;
    
    /** Length of message data in bytes (without this IP header) */
    private int dataLength;
    
    /** Identification */
    private int identification;
    
    /** Fragment offset */
    private int fragmentOffset;
    
    /** Time to live */
    private int ttl;
    
    /** Protocol ID */
    private int protocol;
    
    /** Source address */
    private IPv4Address srcAddress;
    
    /** Destination address */
    private IPv4Address dstAddress;
    
    /** Checksum is ok */
    private final boolean checksumOk;

    /**
     * Create a new instance
     * 
     * @param tos
     * @param ttl
     * @param protocol
     * @param dstAddress
     */
    public IPv4Header(int tos, int ttl, int protocol, IPv4Address dstAddress, int dataLength) {
        if (dstAddress == null) {
            throw new IllegalArgumentException("dstAddress cannot be null");
        }
        this.version = 4;
        this.tos = tos;
        this.ttl = ttl;
        this.protocol = protocol;
        this.dstAddress = dstAddress;
        this.hdrlength = 20;
        this.dataLength = dataLength;
        this.checksumOk = true; // For this constructor not relevant
    }

    /**
     * Create a new instance, read the header from the given buffer
     * 
     * @param skbuf
     */
    public IPv4Header(SocketBuffer skbuf) {
        final int b0 = skbuf.get(0);
        this.version = (b0 >> 4) & 0x0f;
        this.hdrlength = (b0 & 0x0f) * 4;
        this.tos = skbuf.get(1);
        this.dataLength = skbuf.get16(2) - hdrlength;
        this.identification = skbuf.get16(4);
        this.fragmentOffset = skbuf.get16(6);
        this.ttl = skbuf.get(8);
        this.protocol = skbuf.get(9);
        this.srcAddress = new IPv4Address(skbuf, 12);
        this.dstAddress = new IPv4Address(skbuf, 16);

        final int ccs = IPv4Utils.calcChecksum(skbuf, 0, hdrlength);
        checksumOk = (ccs == 0);
    }

    /**
     * Create a clone of the given header
     * 
     * @param src
     */
    public IPv4Header(IPv4Header src) {
        this.version = src.version;
        this.hdrlength = src.hdrlength;
        this.tos = src.tos;
        this.dataLength = src.dataLength;
        this.identification = src.identification;
        this.fragmentOffset = src.fragmentOffset;
        this.ttl = src.ttl;
        this.protocol = src.protocol;
        this.srcAddress = src.srcAddress;
        this.dstAddress = src.dstAddress;
        this.checksumOk = src.checksumOk;
    }

    /**
     * Prefix this header to the front of the given buffer
     * 
     * @param skbuf
     */
    public void prefixTo(SocketBuffer skbuf) {
        skbuf.insert(hdrlength);
        skbuf.set(0, ((version << 4) & 0xf0) | ((hdrlength / 4) & 0xf));
        skbuf.set(1, tos);
        skbuf.set16(2, dataLength + hdrlength);
        skbuf.set16(4, identification);
        skbuf.set16(6, fragmentOffset);
        skbuf.set(8, ttl);
        skbuf.set(9, protocol);
        skbuf.set(10, 0); // checksum, calculate and set later
        srcAddress.writeTo(skbuf, 12);
        dstAddress.writeTo(skbuf, 16);
        // calculate and set checksum
        final int ccs = IPv4Utils.calcChecksum(skbuf, 0, hdrlength);
        if (ccs == 0) {
            skbuf.set(10, 0xffff);
        } else {
            skbuf.set16(10, ccs);
        }
    }

    /**
     * Finalize the header in the given buffer. This method is called when all
     * layers have set their header data and can be used e.g. to update checksum
     * values.
     * 
     * @param skbuf The buffer
     * @param offset The offset to the first byte (in the buffer) of this header
     *            (since low layer headers are already prefixed)
     */
    public void finalizeHeader(SocketBuffer skbuf, int offset) {
        // Nothing to do
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getSourceAddress() {
        return srcAddress;
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getDestinationAddress() {
        return dstAddress;
    }

    /**
     * Gets the destination
     */
    public IPv4Address getDestination() {
        return dstAddress;
    }

    /**
     * Is the don't fragment flag set?
     */
    public boolean isDontFragment() {
        return ((fragmentOffset & IP_DF) != 0);
    }

    /**
     * Is the more fragments flag set?
     */
    public boolean hasMoreFragments() {
        return ((fragmentOffset & IP_MF) != 0);
    }

    /**
     * Is this a fragment. That is, the MF flag is set, or fragment offset is
     * greater then 0.
     */
    public boolean isFragment() {
        return (((fragmentOffset & IP_MF) != 0) || ((fragmentOffset & IP_FRAGOFS_MASK) != 0));
    }

    /**
     * Gets the offset of this fragment in the IP packet
     */
    public int getFragmentOffset() {
        return (fragmentOffset & 0x1FFF) << 3;
    }

    /**
     * Gets the key used to find the correct fragment list. This key contains:
     * identification, protocol, srcAddress, dstAddress
     */
    public Object getFragmentListKey() {
        final StringBuilder b = new StringBuilder();
        b.append(identification);
        b.append('-');
        b.append(protocol);
        b.append('-');
        b.append(srcAddress);
        b.append('-');
        b.append(dstAddress);
        return b.toString();
    }

    /**
     * Set the fragment offset (not the DF & MF bits)
     * 
     * @param i
     */
    public void setFragmentOffset(int i) {
        fragmentOffset &= ~IP_FRAGOFS_MASK;
        fragmentOffset |= ((i >> 3) & IP_FRAGOFS_MASK);
    }

    /**
     * Sets the don't fragment flag
     */
    public void setDontFragment(boolean on) {
        if (on) {
            fragmentOffset |= IP_DF;
        } else {
            fragmentOffset &= ~IP_DF;
        }
    }

    /**
     * Sets the more fragments flag
     */
    public void setMoreFragments(boolean on) {
        if (on) {
            fragmentOffset |= IP_MF;
        } else {
            fragmentOffset &= ~IP_MF;
        }
    }

    /**
     * Get the header length
     */
    public int getLength() {
        return hdrlength;
    }

    /**
     * Gets the identification number
     */
    public int getIdentification() {
        return identification;
    }

    /**
     * Gets the length of the header and data
     */
    public int getTotalLength() {
        return dataLength + hdrlength;
    }

    /**
     * Gets the length of the data (without the IP header)
     */
    public int getDataLength() {
        return dataLength;
    }

    /**
     * Gets the protocol
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Gets the source address
     */
    public IPv4Address getSource() {
        return srcAddress;
    }

    /**
     * Gets the type of service attribute
     */
    public int getTos() {
        return tos;
    }

    /**
     * Gets the time to live attribute
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * Gets the IP version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the destination address
     * 
     * @param address
     */
    public void setDestination(IPv4Address address) {
        dstAddress = address;
    }

    /**
     * Sets the identification number
     * @param i
     */
    public void setIdentification(int i) {
        identification = i;
    }

    /**
     * Sets the length of the data in bytes
     * @param i
     */
    public void setDataLength(int i) {
        dataLength = i;
    }

    /**
     * Sets the protocol
     * @param i
     */
    public void setProtocol(int i) {
        protocol = i;
    }

    /**
     * Sets the source address
     * @param address
     */
    public void setSource(IPv4Address address) {
        srcAddress = address;
    }

    /**
     * Sets the type of service
     * @param i
     */
    public void setTos(int i) {
        tos = i;
    }

    /**
     * Sets the time to live
     * @param i
     */
    public void setTtl(int i) {
        ttl = i;
    }

    /**
     * Is the checksum valid
     */
    public boolean isChecksumOk() {
        return checksumOk;
    }

    /**
     * Swap the source and destination address
     */
    public void swapAddresses() {
        final IPv4Address tmp = dstAddress;
        this.dstAddress = this.srcAddress;
        this.srcAddress = tmp;
    }

}
