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
 
package org.jnode.net.ipv4.udp;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayerHeader;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Utils;
import org.jnode.util.NumberUtils;

/**
 * Header of an UDP packet
 * 
 * @author epr
 */
public class UDPHeader implements TransportLayerHeader, UDPConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(UDPHeader.class);
    private final int srcPort;
    private final int dstPort;
    private final int udpLength;
    private final boolean checksumOk;

    /**
     * Create a new instance
     * 
     * @param srcPort
     * @param dstPort
     * @param dataLength
     */
    public UDPHeader(int srcPort, int dstPort, int dataLength) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.udpLength = UDP_HLEN + dataLength;
        this.checksumOk = true;
    }

    /**
     * Create a new instance and read the contents from the given buffer
     * 
     * @param skbuf
     */
    public UDPHeader(SocketBuffer skbuf) {
        this.srcPort = skbuf.get16(0);
        this.dstPort = skbuf.get16(2);
        this.udpLength = skbuf.get16(4);
        final int checksum = skbuf.get16(6);
        if (checksum == 0) {
            log.debug("No checksum set");
            this.checksumOk = true;
        } else {
            // Create the pseudo header for checksum calculation
            final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
            final SocketBuffer phdr = new SocketBuffer(12);
            phdr.insert(12);
            ipHdr.getSource().writeTo(phdr, 0);
            ipHdr.getDestination().writeTo(phdr, 4);
            phdr.set(8, 0);
            phdr.set(9, ipHdr.getProtocol());
            phdr.set16(10, udpLength);
            phdr.append(skbuf);
            final int ccs2 = IPv4Utils.calcChecksum(phdr, 0, udpLength + 12);
            this.checksumOk = (ccs2 == 0);
            if (!checksumOk) {
                log.debug("Found invalid UDP checksum 0x" + NumberUtils.hex(ccs2, 4));
            }
        }
    }

    /**
     * @see org.jnode.net.LayerHeader#getLength()
     */
    public int getLength() {
        return UDP_HLEN;
    }

    /**
     * @see org.jnode.net.LayerHeader#prefixTo(org.jnode.net.SocketBuffer)
     */
    public void prefixTo(SocketBuffer skbuf) {
        skbuf.insert(UDP_HLEN);
        skbuf.set16(0, srcPort);
        skbuf.set16(2, dstPort);
        skbuf.set16(4, udpLength);
        skbuf.set16(6, 0); // Checksum, calculate and overwrite later
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
        final int ccs = calcChecksum(skbuf, offset);
        skbuf.set16(offset + 6, ccs);
    }

    /**
     * Is the checksum valid?
     */
    public boolean isChecksumOk() {
        return checksumOk;
    }

    /**
     * Gets the destination port
     */
    public int getDstPort() {
        return dstPort;
    }

    /**
     * Gets the source port
     */
    public int getSrcPort() {
        return srcPort;
    }

    /**
     * Gets the length of the UDP packet (header + data)
     */
    public int getUdpLength() {
        return udpLength;
    }

    /**
     * Gets the length of the UDP data.
     */
    public int getDataLength() {
        return udpLength - UDP_HLEN;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "UDP srcPort=" + srcPort + ", dstPort=" + dstPort + ", dataLength=" + getDataLength();
    }

    private int calcChecksum(SocketBuffer skbuf, int offset) {
        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        final SocketBuffer phdr = new SocketBuffer(12);
        phdr.insert(12);
        ipHdr.getSource().writeTo(phdr, 0);
        ipHdr.getDestination().writeTo(phdr, 4);
        phdr.set(8, 0);
        phdr.set(9, ipHdr.getProtocol());
        phdr.set16(10, udpLength);
        phdr.append(offset, skbuf);
        final int csLength = udpLength + 12;
        return IPv4Utils.calcChecksum(phdr, 0, csLength);
    }
}
