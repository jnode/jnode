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

package org.jnode.net.arp;

import java.net.SocketException;

import org.jnode.net.HardwareAddress;
import org.jnode.net.NetworkLayerHeader;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class ARPHeader implements NetworkLayerHeader {

    private HardwareAddress srcHWAddress;
    private ProtocolAddress srcPAddress;
    private HardwareAddress targetHWAddress;
    private ProtocolAddress targetPAddress;
    private int op;
    private final int hwtype;
    private final int ptype;

    /**
     * Create a new instance
     * 
     * @param srcHWAddress
     * @param srcPAddress
     * @param targetHWAddress
     * @param targetPAddress
     * @param op
     * @param hwtype
     * @param ptype
     */
    public ARPHeader(HardwareAddress srcHWAddress, ProtocolAddress srcPAddress,
            HardwareAddress targetHWAddress, ProtocolAddress targetPAddress, int op, int hwtype,
            int ptype) {
        this.srcHWAddress = srcHWAddress;
        this.srcPAddress = srcPAddress;
        this.targetHWAddress = targetHWAddress;
        this.targetPAddress = targetPAddress;
        this.op = op;
        this.hwtype = hwtype;
        this.ptype = ptype;
    }

    /**
     * Create a new packet from a socketbuffer
     * 
     * @param skbuf
     */
    public ARPHeader(SocketBuffer skbuf) throws SocketException {
        hwtype = skbuf.get16(0);
        ptype = skbuf.get16(2);
        // int hwsize = skbuf.get(4);
        // int psize = skbuf.get(5);
        op = skbuf.get16(6);
        if ((hwtype == 1) && (ptype == EthernetConstants.ETH_P_IP)) {
            srcHWAddress = new EthernetAddress(skbuf, 8);
            srcPAddress = new IPv4Address(skbuf, 14);
            targetHWAddress = new EthernetAddress(skbuf, 18);
            targetPAddress = new IPv4Address(skbuf, 24);
        } else {
            throw new SocketException("Unknown hw,ptype: " + hwtype + "," + ptype);
        }
    }

    /**
     * Gets the length of this header in bytes
     */
    public int getLength() {
        return (8 + (srcHWAddress.getLength() + srcPAddress.getLength()) * 2);
    }

    /**
     * Write this packet to the given buffer
     * 
     * @param skbuf
     */
    public void prefixTo(SocketBuffer skbuf) {
        skbuf.insert(8 + (srcHWAddress.getLength() + srcPAddress.getLength()) * 2);
        int ofs = 0;
        skbuf.set16(ofs + 0, hwtype);
        skbuf.set16(ofs + 2, ptype);
        skbuf.set(ofs + 4, srcHWAddress.getLength());
        skbuf.set(ofs + 5, srcPAddress.getLength());
        skbuf.set16(ofs + 6, op);
        ofs += 8;
        srcHWAddress.writeTo(skbuf, ofs);
        ofs += srcHWAddress.getLength();
        srcPAddress.writeTo(skbuf, ofs);
        ofs += srcPAddress.getLength();
        targetHWAddress.writeTo(skbuf, ofs);
        ofs += targetHWAddress.getLength();
        targetPAddress.writeTo(skbuf, ofs);
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
        // Do nothing
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getSourceAddress() {
        return srcPAddress;
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getDestinationAddress() {
        return targetPAddress;
    }

    /**
     * Gets the hardware type
     */
    public int getHType() {
        return hwtype;
    }

    /**
     * Gets the operation
     */
    public int getOperation() {
        return op;
    }

    /**
     * Gets the protocol type
     */
    public int getPType() {
        return ptype;
    }

    /**
     * Gets the source hardware address
     */
    public HardwareAddress getSrcHWAddress() {
        return srcHWAddress;
    }

    /**
     * Gets the source protocol address
     */
    public ProtocolAddress getSrcPAddress() {
        return srcPAddress;
    }

    /**
     * Gets the target hardware address
     */
    public HardwareAddress getTargetHWAddress() {
        return targetHWAddress;
    }

    /**
     * Gets the target protocol address
     */
    public ProtocolAddress getTargetPAddress() {
        return targetPAddress;
    }

    /**
     * Swap the two src and target addresses
     * 
     */
    public void swapAddresses() {
        final HardwareAddress hwTmp = targetHWAddress;
        final ProtocolAddress pTmp = targetPAddress;
        targetHWAddress = srcHWAddress;
        targetPAddress = srcPAddress;
        srcHWAddress = hwTmp;
        srcPAddress = pTmp;
    }

    /**
     * @param i
     */
    public void setOperation(int i) {
        op = i;
    }

    /**
     * @param address
     */
    public void setSrcHWAddress(HardwareAddress address) {
        srcHWAddress = address;
    }

    /**
     * @param address
     */
    public void setSrcPAddress(ProtocolAddress address) {
        srcPAddress = address;
    }

}
