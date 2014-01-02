/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

    private static final int ARP_DATA_LENGTH = 28;

    private HardwareAddress sourceHardwareAddress;
    private ProtocolAddress sourceProtocolAddress;
    private HardwareAddress destinationHardwareAddress;
    private ProtocolAddress destinationProtocolAddress;
    private ARPOperation operation;
    private final int hardwareType;
    private final int protocolType;
    private int hardwareAddressSize;
    private int protocolAddressSize;

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
                     HardwareAddress targetHWAddress, ProtocolAddress targetPAddress, ARPOperation op, int hwtype,
                     int ptype, int hwSize, int pSize) {
        this.sourceHardwareAddress = srcHWAddress;
        this.sourceProtocolAddress = srcPAddress;
        this.destinationHardwareAddress = targetHWAddress;
        this.destinationProtocolAddress = targetPAddress;
        this.operation = op;
        this.hardwareType = hwtype;
        this.protocolType = ptype;
        this.hardwareAddressSize = hwSize;
        this.protocolAddressSize = pSize;
    }

    /**
     * Create a new packet from a socketbuffer
     *
     * @param skbuf
     */
    public ARPHeader(SocketBuffer skbuf) throws SocketException {
        hardwareType = skbuf.get16(0);
        protocolType = skbuf.get16(2);
        hardwareAddressSize = skbuf.get(4);
        protocolAddressSize = skbuf.get(5);
        operation = ARPOperation.getType(skbuf.get16(6));
        if ((hardwareType == 1) && (protocolType == EthernetConstants.ETH_P_IP)) {
            sourceHardwareAddress = new EthernetAddress(skbuf, 8);
            sourceProtocolAddress = new IPv4Address(skbuf, 14);
            destinationHardwareAddress = new EthernetAddress(skbuf, 18);
            destinationProtocolAddress = new IPv4Address(skbuf, 24);
        } else {
            throw new SocketException("Unknown hw,ptype: " + hardwareType + ',' + protocolType);
        }
    }

    /**
     * Gets the length of this header in bytes
     */
    public int getLength() {
        return (8 + (sourceHardwareAddress.getLength() + sourceProtocolAddress.getLength()) * 2);
    }

    /**
     * Write this packet to the given buffer
     *
     * @param skbuf
     */
    public void prefixTo(SocketBuffer skbuf) {
        skbuf.insert(8 + (sourceHardwareAddress.getLength() + sourceProtocolAddress.getLength()) * 2);
        int ofs = 0;
        skbuf.set16(ofs + 0, hardwareType);
        skbuf.set16(ofs + 2, protocolType);
        skbuf.set(ofs + 4, sourceHardwareAddress.getLength());
        skbuf.set(ofs + 5, sourceProtocolAddress.getLength());
        skbuf.set16(ofs + 6, operation.getId());
        ofs += 8;
        sourceHardwareAddress.writeTo(skbuf, ofs);
        ofs += sourceHardwareAddress.getLength();
        sourceProtocolAddress.writeTo(skbuf, ofs);
        ofs += sourceProtocolAddress.getLength();
        destinationHardwareAddress.writeTo(skbuf, ofs);
        ofs += destinationHardwareAddress.getLength();
        destinationProtocolAddress.writeTo(skbuf, ofs);
    }

    /**
     * Finalize the header in the given buffer. This method is called when all
     * layers have set their header data and can be used e.g. to update checksum
     * values.
     *
     * @param skbuf  The buffer
     * @param offset The offset to the first byte (in the buffer) of this header
     *               (since low layer headers are already prefixed)
     */
    public void finalizeHeader(SocketBuffer skbuf, int offset) {
        // Do nothing
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getSourceAddress() {
        return sourceProtocolAddress;
    }

    /**
     * Gets the source address of the packet described in this header
     */
    public ProtocolAddress getDestinationAddress() {
        return destinationProtocolAddress;
    }

    public int getDataLength() {
        return ARP_DATA_LENGTH;
    }

    /**
     * Gets the hardware type
     */
    public int getHType() {
        return hardwareType;
    }

    /**
     * Gets the operation
     */
    public ARPOperation getOperation() {
        return operation;
    }

    /**
     * Gets the protocol type
     */
    public int getPType() {
        return protocolType;
    }

    /**
     * Gets the source hardware address
     */
    public HardwareAddress getSrcHWAddress() {
        return sourceHardwareAddress;
    }

    /**
     * Gets the source protocol address
     */
    public ProtocolAddress getSrcPAddress() {
        return sourceProtocolAddress;
    }

    /**
     * Gets the target hardware address
     */
    public HardwareAddress getTargetHWAddress() {
        return destinationHardwareAddress;
    }

    /**
     * Gets the target protocol address
     */
    public ProtocolAddress getTargetPAddress() {
        return destinationProtocolAddress;
    }

    public int getHardwareAddressSize() {
        return hardwareAddressSize;
    }

    public int getProtocolAddressSize() {
        return protocolAddressSize;
    }

    /**
     * Swap the two src and target addresses
     */
    public void swapAddresses() {
        final HardwareAddress hwTmp = destinationHardwareAddress;
        final ProtocolAddress pTmp = destinationProtocolAddress;
        destinationHardwareAddress = sourceHardwareAddress;
        destinationProtocolAddress = sourceProtocolAddress;
        sourceHardwareAddress = hwTmp;
        sourceProtocolAddress = pTmp;
    }


    /**
     * @param operation
     */
    public void setOperation(ARPOperation operation) {
        this.operation = operation;
    }

    /**
     * @param address
     */
    public void setSrcHWAddress(HardwareAddress address) {
        sourceHardwareAddress = address;
    }

    /**
     * @param address
     */
    public void setSrcPAddress(ProtocolAddress address) {
        sourceProtocolAddress = address;
    }

}
