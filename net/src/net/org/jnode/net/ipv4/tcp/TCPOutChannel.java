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

package org.jnode.net.ipv4.tcp;

import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.jnode.net.ipv4.IPv4Header;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPOutChannel {
    private static final boolean DEBUG = false;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TCPOutChannel.class);

    /**
     * The protocol
     */
    private final TCPProtocol tcp;

    /**
     * All unacked segments
     */
    private final LinkedList<TCPOutSegment> unackedSegments = new LinkedList<TCPOutSegment>();

    /**
     * The outgoing databuffer
     */
    private final TCPDataBuffer dataBuffer;

    /**
     * Send unacknowledged
     */
    private int snd_unack;

    /**
     * Send next seq-nr
     */
    private int snd_next;

    /**
     * Highest seq-nr sent; used to recognize retransmits
     */
    private int snd_max;

    /**
     * Maximum segment size (determined by the foreign part of the connection
     */
    private int mss;

    /**
     * The control block I belong to
     */
    private final TCPControlBlock controlBlock;

    /**
     * Number of ticks before a retransmit timeout
     */
    private int timeoutTicks = 6;

    /**
     * Create a new instance
     */
    public TCPOutChannel(TCPProtocol tcp, TCPControlBlock controlBlock, int isn) {
        this.tcp = tcp;
        this.controlBlock = controlBlock;
        this.dataBuffer = new TCPDataBuffer(TCPConstants.TCP_BUFFER_SIZE);
        this.snd_unack = isn;
        this.snd_next = isn;
        this.snd_max = isn;
        this.mss = TCPConstants.TCP_DEFAULT_MSS;
    }

    /**
     * Process an ack-nr.
     * Remove all segments that have been acknowledged and remove
     * the occupied data from the databuffer.
     *
     * @param ackNr
     */
    public synchronized void processAck(int ackNr) {
        // Is the ack valid?
        if (snd_unack == ackNr) {
            // Not a new ack, ignore it
            return;
        } else if (!TCPUtils.SEQ_LT(snd_unack, ackNr)) {
            // snd_unack < ackNr violated
            log.debug("snd_unack < ackNr violated");
            return;
        }
        if (!TCPUtils.SEQ_LE(ackNr, snd_max)) {
            // ackNr <= snd_max violated
            log.debug("ackNr <= snd_max violated");
            return;
        }

        // The ackNr is valid
        final int diff = ackNr - snd_unack;
        snd_unack = ackNr;
        // Remove data from the databuffer
        dataBuffer.pull(diff);
        for (Iterator<TCPOutSegment> i = unackedSegments.iterator(); i.hasNext();) {
            final TCPOutSegment seg = (TCPOutSegment) i.next();
            final int seqNr = seg.getSeqNr();
            if (TCPUtils.SEQ_LT(seqNr, ackNr)) {
                // Remove the segment
                i.remove();
            } else {
                // Adjust the dataOffset
                seg.adjustDataOffset(diff);
            }
        }
        // Notify any blocked threads
        notifyAll();
    }

    /**
     * Process timeout handling
     */
    public void timeout() throws SocketException {
        //allocation free looping
        for (int i = 0; i < unackedSegments.size(); i++) {
            TCPOutSegment seg = unackedSegments.get(i);
            seg.timeout(tcp);
        }
    }

    public int getBufferSize() {
        return dataBuffer.getLength();
    }

    /**
     * Send a TCP segment containing no data
     *
     * @param ipHdr
     * @param hdr
     */
    public void send(IPv4Header ipHdr, TCPHeader hdr) throws SocketException {
        // Check the datalength
        if (hdr.getDataLength() != 0) {
            throw new IllegalArgumentException("dataLength must be 0");
        }
        // Do the actual send
        sendHelper(ipHdr, hdr, 0);
    }

    /**
     * Send a TCP segment containing the given data.
     * This method blocks until there is enough space in the output buffer
     * to hold the data.
     *
     * @param ipHdr
     * @param hdr
     * @param data
     * @param offset
     * @param length Must be smaller or equal to mss.
     */
    public synchronized void send(IPv4Header ipHdr, TCPHeader hdr, byte[] data, int offset,
                                  int length) throws SocketException {
        if (DEBUG) {
            log.debug("outChannel.send(ipHdr,hdr,data," + offset + ", " + length + ")");
        }
        // Check for maximum datalength
        if (length > mss) {
            throw new IllegalArgumentException("dataLength must be <= mss");
        }
        // Wait until there is space in the output buffer
        while ((length > dataBuffer.getFreeSize()) && !controlBlock.isReset()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
        if (controlBlock.isReset()) {
            throw new SocketException("Connection reset");
        }
        // Add to databuffer
        final int bufOfs = dataBuffer.add(data, offset, length);
        // Update tcp header
        hdr.setDataLength(length);
        // Do the actual send
        sendHelper(ipHdr, hdr, bufOfs);
    }

    /**
     * Do the actual sending and adjusting of sequence number.
     *
     * @param ipHdr
     * @param hdr
     * @param dataOffset
     */
    private void sendHelper(IPv4Header ipHdr, TCPHeader hdr, int dataOffset)
        throws SocketException {
        // Adjust the sequence numbers
        hdr.setSequenceNr(snd_next);
        if (hdr.isFlagSynchronizeSet() || hdr.isFlagFinishedSet()) {
            snd_next++;
            //snd_unack++;
        } else {
            snd_next += hdr.getDataLength();
        }
        snd_max = snd_next;
        // Create & send the segment
        final TCPOutSegment seg =
            new TCPOutSegment(ipHdr, hdr, dataBuffer, dataOffset, timeoutTicks);
        seg.send(tcp);
        if (!seg.isAckOnly() && !hdr.isFlagSynchronizeSet()) {
            if (DEBUG) {
                log.debug("Adding segment " + seg.getSeqNr() + " to unacklist");
            }
            unackedSegments.add(seg);
        }
    }

    /**
     * Notify a connection reset
     */
    public synchronized void notifyConnectionReset() {
        notifyAll();
    }

    /**
     * @return Returns the mss.
     */
    public final int getMss() {
        return this.mss;
    }

    /**
     * @param mss The mss to set.
     */
    public final void setMss(int mss) {
        this.mss = mss;
    }

}
