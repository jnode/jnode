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
 
package org.jnode.net.ipv4.tcp;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayerHeader;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Utils;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class TCPHeader implements TransportLayerHeader, TCPConstants {

    private static final Logger log = Logger.getLogger(TCPHeader.class);
    private final int srcPort;
    private final int dstPort;
    private int sequenceNr;
    private final int ackNr;
    private final int headerLength;
    private int flags;
    private int tcpLength;
    private final int windowSize;
    private final int urgentPointer;
    private final boolean checksumOk;

    /**
     * Create a new instance
     * 
     * @param srcPort
     * @param dstPort
     * @param tcpLength
     * @param seqNr
     * @param ackNr
     * @param windowSize
     * @param urgentPointer
     */
    public TCPHeader(int srcPort, int dstPort, int tcpLength, int seqNr, int ackNr, int windowSize,
            int urgentPointer) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.tcpLength = tcpLength;
        this.sequenceNr = seqNr;
        this.ackNr = ackNr;
        this.headerLength = TCP_HLEN;
        this.flags = 0;
        this.windowSize = windowSize;
        this.urgentPointer = urgentPointer;
        this.checksumOk = true;
    }

    /**
     * Create a new instance and read the contents from the given buffer
     * 
     * @param skbuf
     */
    public TCPHeader(SocketBuffer skbuf) {
        this.srcPort = skbuf.get16(0);
        this.dstPort = skbuf.get16(2);
        this.sequenceNr = skbuf.get32(4);
        this.ackNr = skbuf.get32(8);

        final int optionHdrLength = skbuf.get16(12);
        this.headerLength = (optionHdrLength & 0xf000) >> 10;
        this.flags = optionHdrLength & 0x0FFF;

        // Syslog.debug("optionHdrLength 0x" + NumberUtils.hex(optionHdrLength,
        // 4));

        this.windowSize = skbuf.get16(14);

        final int checksum = skbuf.get16(16);
        this.urgentPointer = skbuf.get16(18);

        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        this.tcpLength = ipHdr.getDataLength() - headerLength;

        if (checksum == 0) {
            log.debug("No checksum set");
            this.checksumOk = true;
        } else {
            // Create the pseudo header for checksum calculation
            final SocketBuffer phdr = new SocketBuffer(12);
            phdr.insert(12);
            ipHdr.getSource().writeTo(phdr, 0);
            ipHdr.getDestination().writeTo(phdr, 4);
            phdr.set(8, 0);
            phdr.set(9, ipHdr.getProtocol());
            phdr.set16(10, tcpLength + headerLength);
            phdr.append(skbuf);

            /*
             * final int ccs1 = IPv4Utils.calcChecksum(phdr, 0, 12); final int
             * ccs2 = IPv4Utils.calcChecksum(skbuf, 0, udpLength, ccs1);
             */
            final int ccs2 = IPv4Utils.calcChecksum(phdr, 0, headerLength + tcpLength + 12);
            this.checksumOk = (ccs2 == 0);
            if (!checksumOk) {
                log.debug("Found invalid TCP checksum 0x" + NumberUtils.hex(ccs2, 4) +
                        ", tcpLength 0x" + NumberUtils.hex(tcpLength, 4) + ", ipDataLength 0x" +
                        NumberUtils.hex(ipHdr.getDataLength(), 4) + ", tcpHdrLen 0x" +
                        NumberUtils.hex(headerLength, 4));
            }
        }
    }

    /**
     * @see org.jnode.net.LayerHeader#getLength()
     */
    public int getLength() {
        return headerLength;
    }

    /**
     * Gets the length of the TCP data.
     */
    public int getDataLength() {
        return tcpLength;
    }

    /**
     * Sets the length of the TCP data.
     */
    public void setDataLength(int length) {
        this.tcpLength = length;
    }

    /**
     * @see org.jnode.net.LayerHeader#prefixTo(org.jnode.net.SocketBuffer)
     */
    public void prefixTo(SocketBuffer skbuf) {
        skbuf.insert(headerLength);
        skbuf.set16(0, srcPort);
        skbuf.set16(2, dstPort);
        skbuf.set32(4, sequenceNr);
        skbuf.set32(8, ackNr);
        skbuf.set16(12, ((headerLength << 10) & 0xf000) | (flags & 0x0FFF));
        skbuf.set16(14, windowSize);
        skbuf.set16(16, 0); // Checksum, calculate and overwrite later
        skbuf.set16(18, urgentPointer);
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
        skbuf.set16(offset + 16, 0);
        final int ccs = calcChecksum(skbuf, offset);
        skbuf.set16(offset + 16, ccs);
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
     * Is the URG flag set?
     */
    public boolean isFlagUrgentSet() {
        return ((flags & TCPF_URG) != 0);
    }

    /**
     * Is the ACK flag set?
     */
    public boolean isFlagAcknowledgeSet() {
        return ((flags & TCPF_ACK) != 0);
    }

    /**
     * Is the PSH flag set?
     */
    public boolean isFlagPushSet() {
        return ((flags & TCPF_PSH) != 0);
    }

    /**
     * Is the RST flag set?
     */
    public boolean isFlagResetSet() {
        return ((flags & TCPF_RST) != 0);
    }

    /**
     * Is the Synchronize Sequence Numbers flag set?
     */
    public boolean isFlagSynchronizeSet() {
        return ((flags & TCPF_SYN) != 0);
    }

    /**
     * Is the Finished flag set?
     */
    public boolean isFlagFinishedSet() {
        return ((flags & TCPF_FIN) != 0);
    }

    public final int getFlags() {
        return flags;
    }

    public String getFlagsAsString() {
        final StringBuilder b = new StringBuilder(4);
        if (isFlagSynchronizeSet()) {
            b.append('S');
        }
        if (isFlagFinishedSet()) {
            b.append('F');
        }
        if (isFlagResetSet()) {
            b.append('R');
        }
        if (isFlagUrgentSet()) {
            b.append('P');
        }
        if (b.length() == 0) {
            return ".";
        } else {
            return b.toString();
        }
    }

    /**
     * Set a given flag(s)
     * 
     * @param flag
     */
    public void setFlags(int flag) {
        this.flags |= flag;
    }

    /**
     * Reset a given flag(s)
     * 
     * @param flag
     */
    public void resetFlags(int flag) {
        this.flags &= ~flag;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(srcPort);
        b.append(" > ");
        b.append(dstPort);
        b.append(": ");
        b.append(getFlagsAsString());
        b.append(" ");
        b.append(sequenceNr & 0xFFFFFFFFL);
        b.append(":");
        b.append((sequenceNr + tcpLength) & 0xFFFFFFFFL);
        b.append("(");
        b.append(tcpLength);
        b.append(")");
        if (isFlagAcknowledgeSet()) {
            b.append(", ack ");
            b.append(ackNr & 0xFFFFFFFFL);
        }
        b.append(", win ");
        b.append(windowSize);
        return b.toString();
    }

    /**
     * Gets the acknowledgment number
     */
    public int getAckNr() {
        return ackNr;
    }

    /**
     * Gets the option flags
     */
    public int getOptions() {
        return flags;
    }

    /**
     * Gets the sequence number
     */
    public int getSequenceNr() {
        return sequenceNr;
    }

    /**
     * Gets the length of the TCP Data in bytes.
     */
    public int getTcpLength() {
        return tcpLength;
    }

    /**
     * Gets the urgent pointer
     */
    public int getUrgentPointer() {
        return urgentPointer;
    }

    /**
     * Gets the window size
     */
    public int getWindowSize() {
        return windowSize;
    }

    private int calcChecksum(SocketBuffer skbuf, int offset) {
        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        final SocketBuffer phdr = new SocketBuffer(12);
        phdr.insert(12);
        ipHdr.getSource().writeTo(phdr, 0);
        ipHdr.getDestination().writeTo(phdr, 4);
        phdr.set(8, 0);
        phdr.set(9, ipHdr.getProtocol());
        phdr.set16(10, tcpLength + headerLength);
        phdr.append(offset, skbuf);
        final int csLength = headerLength + tcpLength + 12;
        return IPv4Utils.calcChecksum(phdr, 0, csLength);
    }

    /**
     * @param sequenceNr
     *            The sequenceNr to set.
     */
    protected final void setSequenceNr(int sequenceNr) {
        this.sequenceNr = sequenceNr;
    }

}
