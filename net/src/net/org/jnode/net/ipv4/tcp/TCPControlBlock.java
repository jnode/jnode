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

import java.net.ConnectException;
import java.net.SocketException;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4ControlBlock;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPControlBlock extends IPv4ControlBlock implements TCPConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TCPControlBlock.class);

    /**
     * The outgoing channel
     */
    private final TCPOutChannel outChannel;

    /**
     * The incoming channel
     */
    private final TCPInChannel inChannel;

    /** Last incoming sequence number */
    // private int lastInSeqNr;
    
    /**
     * Window size of the outgoing connection
     */
    private int outWindowSize;

    /**
     * The current state
     */
    private int curState;

    /**
     * My listening parent
     */
    private final TCPControlBlock parent;

    /**
     * List of connections that are established, but have not been "accepted"
     */
    private LinkedList<TCPControlBlock> readyToAcceptList = new LinkedList<TCPControlBlock>();

    /**
     * Has this connection be reset?
     */
    private boolean reset;

    /**
     * Has this connection been refused?
     */
    private boolean refused;

    /**
     * Timeout for blocking operations
     */
    private int timeout = TCP_DEFAULT_TIMEOUT;

    /**
     * Create a new instance
     * 
     * @param list
     * @param parent
     * @param tcp
     * @param isn The initial outgoing sequence number
     */
    public TCPControlBlock(TCPControlBlockList list, TCPControlBlock parent, TCPProtocol tcp,
            int isn) {
        super(list, IPv4Constants.IPPROTO_TCP, TCP_DEFAULT_TTL);
        this.parent = parent;
        this.outChannel = new TCPOutChannel(tcp, this, isn);
        this.inChannel = new TCPInChannel(this);
        // this.tcp = tcp;
        this.curState = TCPS_CLOSED;
        this.outWindowSize = TCP_MAXWIN;
        this.reset = false;
        this.refused = false;
    }

    // ------------------------------------------
    // Receive (input) methods
    // ------------------------------------------

    /**
     * Handle a received segment for this connection
     * 
     * @param hdr
     * @param skbuf
     */
    public synchronized void receive(TCPHeader hdr, SocketBuffer skbuf) throws SocketException {
        if (log.isDebugEnabled()) {
            log.debug("receive: me=[" + this + "], hdr=[" + hdr + "]");
        }

        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        final boolean ack = hdr.isFlagAcknowledgeSet();
        final boolean rst = hdr.isFlagResetSet();

        if (rst) {
            receiveProcessReset(ipHdr, hdr, skbuf);
            return;
        }

        if (ack) {
            outChannel.processAck(hdr.getAckNr());
        }

        switch (curState) {
            case TCPS_LISTEN:
                receiveListen(ipHdr, hdr, skbuf);
                break;
            case TCPS_SYN_RECV:
                receiveSynRecv(ipHdr, hdr, skbuf);
                break;
            case TCPS_SYN_SENT:
                receiveSynSend(ipHdr, hdr, skbuf);
                break;
            case TCPS_ESTABLISHED:
                receiveEstablished(ipHdr, hdr, skbuf);
                break;
            case TCPS_FIN_WAIT_1:
                receiveFinWait1(ipHdr, hdr, skbuf);
                break;
            case TCPS_FIN_WAIT_2:
                receiveFinWait2(ipHdr, hdr, skbuf);
                break;
            case TCPS_LAST_ACK:
                receiveLastAck(ipHdr, hdr, skbuf);
                break;
            case TCPS_CLOSING:
                receiveClosing(ipHdr, hdr, skbuf);
                break;
            case TCPS_TIME_WAIT:
                receiveTimeWait(ipHdr, hdr, skbuf);
                break;
            default:
                log.debug("Unhandled state in receive (" + getStateName() + ")");
                break;
        }
    }

    /**
     * Process a reset segment
     * 
     * @param ipHdr
     * @param hdr
     * @param skbuf
     */
    private final void receiveProcessReset(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean ack = hdr.isFlagAcknowledgeSet();
        final boolean syn = hdr.isFlagSynchronizeSet();

        switch (curState) {
            case TCPS_SYN_RECV:
            case TCPS_SYN_SENT:
                notifyConnectionRefused();
                setState(TCPS_CLOSED);
                drop(ipHdr, hdr, "connection refused");
                break;

            case TCPS_ESTABLISHED:
            case TCPS_FIN_WAIT_1:
            case TCPS_FIN_WAIT_2:
            case TCPS_CLOSE_WAIT:
                notifyConnectionReset();
                setState(TCPS_CLOSED);
                drop(ipHdr, hdr, "connection reset");
                break;

            case TCPS_CLOSING:
            case TCPS_LAST_ACK:
            case TCPS_TIME_WAIT:
                setState(TCPS_CLOSED);
                drop(ipHdr, hdr, "connection reset");
                break;
        }

        if (syn) {
            notifyConnectionReset();
            sendRST();
            drop(ipHdr, hdr, "connection reset");
        }

        if (!ack) {
            drop(ipHdr, hdr, "ACK expected together with RST");
        }
    }

    /**
     * Current state is LISTEN. If a SYN segment is received, send a SYN&ACK,
     * and set the state to SYN_RECV
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveListen(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        log.debug("receiveListen");

        final boolean ack = hdr.isFlagAcknowledgeSet();
        final boolean syn = hdr.isFlagSynchronizeSet();

        if (ack) {
            // Drop this segment with a RST reply
            sendRST();
            drop(ipHdr, hdr, "unexpected ACK segment");
        } else if (syn) {
            // Drop if broadcast or multicast
            final IPv4Address dst = ipHdr.getDestination();
            if (dst.isBroadcast() || dst.isMulticast()) {
                // Drop this segment
                drop(ipHdr, hdr, "broadcast or multicast destination");
            } else {
                // Process the SYN request
                final TCPControlBlock copy =
                        (TCPControlBlock) copyAndConnect(dst, ipHdr.getSource(), hdr.getSrcPort());
                copy.listenSynReceivedOnCopy(hdr, skbuf);
            }
        } else {
            // Invalid segment
            drop(ipHdr, hdr, "unexpected segment; SYN expected");
        }
    }

    /**
     * Current state is 0, SYN segment received. This method is called on a copy
     * of the listening control block. Send a SYN&ACK, and set the state to
     * SYN_RECV
     * 
     * @param hdr
     * @param skbuf
     */
    private final void listenSynReceivedOnCopy(TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        log.debug("listenSynReceivedOnCopy");

        // Save the foreign seq nr
        inChannel.initISN(hdr);

        // Send the SYN&ACK TCP reply
        sendACK(TCPF_SYN, hdr.getSequenceNr() + 1);

        // Update the state
        setState(TCPS_SYN_RECV);
    }

    /**
     * Current state is SYN_RECV.
     */
    private final void receiveSynRecv(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean ack = hdr.isFlagAcknowledgeSet();

        if (ack) {
            setState(TCPS_ESTABLISHED);
            parent.notifyChildEstablished(this);
        } else {
            // Invalid segment
            drop(ipHdr, hdr, "ACK expected");
        }
    }

    /**
     * Current state is SYN_SEND.
     */
    private final void receiveSynSend(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean syn = hdr.isFlagSynchronizeSet();

        if (!syn) {
            // Invalid segment
            drop(ipHdr, hdr, "SYN expected");
        } else {
            // Active open , go to ESTABLISHED
            inChannel.initISN(hdr);
            sendACK(0, hdr.getSequenceNr() + 1);
            setState(TCPS_ESTABLISHED);
        }
    }

    /**
     * Current state is ESTABLISHED, FIN segment received. Send a ACK, and set
     * the state to CLOSE_WAIT
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveEstablished(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean fin = hdr.isFlagFinishedSet();
        // Process the data
        inChannel.processData(ipHdr, hdr, skbuf);
        // FIN received, then change state
        if (fin) {
            setState(TCPS_CLOSE_WAIT);
        }
    }

    /**
     * State is FIN_WAIT_1, any segment received
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveFinWait1(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean fin = hdr.isFlagFinishedSet();
        final boolean ack = hdr.isFlagAcknowledgeSet();
        // Process the data
        inChannel.processData(ipHdr, hdr, skbuf);
        // Update state (if required)
        if (fin && ack) {
            setState(TCPS_TIME_WAIT);
        } else if (fin) {
            setState(TCPS_CLOSING);
        } else if (ack) {
            setState(TCPS_FIN_WAIT_2);
        } else {
            // Invalid segment
            drop(ipHdr, hdr, "FIN and/or ACK expected");
        }
    }

    /**
     * State is FIN_WAIT_2, any segment received
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveFinWait2(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean fin = hdr.isFlagFinishedSet();
        // Process the data
        inChannel.processData(ipHdr, hdr, skbuf);
        // Update state
        if (fin) {
            setState(TCPS_TIME_WAIT);

            // wait for 2MSL (2 maximum segment life time)
            final long start = System.currentTimeMillis();
            int timeout = 400;
            long now;

            while (true) {
                now = System.currentTimeMillis();
                if ((start + timeout <= now)) {
                    // We have a timeout
                    break;
                }
                try {
                    wait(10);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
            setState(TCPS_CLOSED);

        } else {
            // Invalid segment
            drop(ipHdr, hdr, "FIN expected");
        }
    }

    /**
     * State is LAST_ACK, any segment received
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveLastAck(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean ack = hdr.isFlagAcknowledgeSet();
        if (ack) {
            setState(TCPS_CLOSED);
        } else {
            // Invalid segment
            drop(ipHdr, hdr, "ACK expected");
        }
    }

    /**
     * State is CLOSING, any segment received
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveClosing(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        final boolean ack = hdr.isFlagAcknowledgeSet();
        if (ack) {
            setState(TCPS_TIME_WAIT);
        } else {
            // Invalid segment
            drop(ipHdr, hdr, "ACK expected");
        }
    }

    /**
     * State is TIME_WAIT, discard any segments
     * 
     * @param hdr
     * @param skbuf
     */
    private final void receiveTimeWait(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf)
        throws SocketException {
        setState(TCPS_CLOSED);
        drop(ipHdr, hdr, "discard all in TIME_WAIT state");
    }

    // ------------------------------------------
    // Timeout methods
    // ------------------------------------------

    /**
     * Process timeout handling
     */
    public void timeout() {
        try {
            outChannel.timeout();
        } catch (SocketException ex) {
            log.error("Error in timeout of " + this, ex);
        }
    }

    // ------------------------------------------
    // Utility methods
    // ------------------------------------------

    /**
     * Notify a segment drop to the debug log
     */
    private void drop(IPv4Header ipHdr, TCPHeader hdr, String reason) {
        log.debug("Dropping segment due to: " + reason);
    }

    /**
     * Send a ACK segment
     */
    protected final void sendACK(int extraFlags, int ackNr) throws SocketException {
        log.debug("sendACK(0x" + NumberUtils.hex(extraFlags, 4) + ", " + (ackNr & 0xFFFFFFFFL) +
                ")");

        // Create the FIN TCP reply
        final TCPHeader replyHdr = createOutgoingTCPHeader(extraFlags | TCPF_ACK, ackNr);
        // ACK takes 0 seq-nrs, so don't increment snd_next

        // Create the IP reply header
        final IPv4Header replyIp = createOutgoingIPv4Header();

        // Send the reply
        outChannel.send(replyIp, replyHdr);
    }

    /**
     * Send a FIN segment
     */
    private final void sendFIN() throws SocketException {
        log.debug("sendFIN");

        // Create the FIN TCP reply
        final TCPHeader replyHdr =
                createOutgoingTCPHeader(TCPF_FIN | TCPF_ACK, inChannel.getRcvNext());

        // Create the IP reply header
        final IPv4Header replyIp = createOutgoingIPv4Header();

        // Send the reply
        outChannel.send(replyIp, replyHdr);
    }

    /**
     * Send a RST segment
     */
    private final void sendRST() throws SocketException {
        log.debug("sendRST");

        // Create the RST TCP reply
        final TCPHeader replyHdr = createOutgoingTCPHeader(TCPF_RST, 0);
        // RST takes 0 seg-nrs TODO is this correct????

        // Create the IP reply header
        final IPv4Header replyIp = createOutgoingIPv4Header();

        // Send the reply
        outChannel.send(replyIp, replyHdr);
    }

    /**
     * Send a SYN segment
     */
    private final void sendSYN() throws SocketException {
        log.debug("sendSYN");

        // Create the SYN TCP
        final TCPHeader hdr = createOutgoingTCPHeader(TCPF_SYN, 0);

        // Create the IP reply header
        final IPv4Header ipHdr = createOutgoingIPv4Header();

        // Send the reply
        outChannel.send(ipHdr, hdr);
    }

    /**
     * Notify this listening parent that one of my children have established a
     * connection.
     * 
     * @param child
     */
    private synchronized void notifyChildEstablished(TCPControlBlock child) throws SocketException {
        if (isState(TCPS_LISTEN)) {
            // Put on the waiting list for accept to handle and notify blocked
            // threads.
            readyToAcceptList.add(child);
            notifyAll();
        } else {
            // I'm not listening anymore, close the connection.
            child.appClose();
        }
    }

    /**
     * Notify a connection reset
     */
    private synchronized void notifyConnectionReset() {
        this.reset = true;
        inChannel.notifyConnectionReset();
        outChannel.notifyConnectionReset();
        notifyAll();
    }

    /**
     * Notify a connection refused
     */
    private void notifyConnectionRefused() {
        this.refused = true;
        notifyAll();
    }

    /**
     * Is the current state equal to the given state?
     * 
     * @param state
     * @return
     */
    private boolean isState(int state) {
        return (this.curState == state);
    }

    /**
     * Update the state and notify any waiting threads
     * 
     * @param state
     */
    private synchronized void setState(int state) throws SocketException {
        // System.out.println("state = " + state);
        if (this.curState != state) {
            this.curState = state;
            if (state == TCPS_CLOSED) {
                super.removeFromList();
            }
            notifyAll();
        }
    }

    /**
     * Wait until the state is equal to the given state or the connection is
     * reset or refused.
     */
    private synchronized void waitUntilState(int state, long timeout) throws TimeoutException {
        final long start = System.currentTimeMillis();
        while (!isState(state) && !isReset() && !isRefused()) {
            final long now = System.currentTimeMillis();
            if ((start + timeout <= now) && (timeout > 0)) {
                // We have a timeout
                throw new TimeoutException();
            }
            try {
                // Thread.currentThread().sleep(Math.max(1, timeout - (now -
                // start)));
                wait(Math.max(1, timeout - (now - start)));
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
    }

    /**
     * Create a TCP header for outgoing trafic
     * 
     * @param options
     * @return The created TCP header
     */
    protected TCPHeader createOutgoingTCPHeader(int options, int ackNr) {
        final TCPHeader hdr =
                new TCPHeader(getLocalPort(), getForeignPort(), 0, 0, ackNr, outWindowSize, 0);
        hdr.setFlags(options);
        return hdr;
    }

    // ------------------------------------------
    // Application methods
    // ------------------------------------------

    /**
     * Wait for incoming requests
     * 
     * @throws SocketException
     */
    public synchronized void appListen() throws SocketException {
        if (!isState(TCPS_CLOSED)) {
            throw new SocketException("Invalid connection state " + getStateName());
        }
        setState(TCPS_LISTEN);
    }

    /**
     * Active connect to a foreign address. This method blocks until the
     * connection has been established.
     * 
     * @throws SocketException
     */
    public synchronized void appConnect(IPv4Address fAddr, int fPort) throws SocketException {
        if (!isState(TCPS_CLOSED)) {
            throw new SocketException("Invalid connection state " + getStateName());
        }
        super.connect(getLocalAddress(), fAddr, fPort);
        for (int attempt = 0; attempt < TCP_MAXCONNECT; attempt++) {
            try {
                // Send the SYN
                sendSYN();
                // Update the state
                setState(TCPS_SYN_SENT);
                // Wait for an ESTABLISHED state
                waitUntilState(TCPS_ESTABLISHED, timeout);
                // Check for reset condition
                if (isRefused()) {
                    throw new ConnectException("Connection refused");
                }
                log.debug("Connected to " + fAddr + ":" + fPort);
                return;
            } catch (TimeoutException ex) {
                // Ignore and just try again
            }
        }
        // Not succeeded to connect
        throw new ConnectException("Connection request timeout");
    }

    /**
     * Wait for an established connection.
     * 
     * @return The accepted connection
     */
    public synchronized TCPControlBlock appAccept() {
        while (true) {
            if (!readyToAcceptList.isEmpty()) {
                final TCPControlBlock child = (TCPControlBlock) readyToAcceptList.getFirst();
                readyToAcceptList.remove(child);
                return child;
            } else {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Active close the connection by the application.
     */
    public/* synchronized */void appClose() throws SocketException {
        if (log.isDebugEnabled()) {
            log.debug("active close state=" + getStateName());
        }

        try {
            switch (curState) {
                case TCPS_SYN_RECV:
                case TCPS_ESTABLISHED: 
                    sendFIN();
                    setState(TCPS_FIN_WAIT_1);

                    // this is blocking the closing of the socket/output stream
                    // - Martin Husted Hartvig 01/03/2005
                    // the waitUntilState have to rethinked
                    // waitUntilState(TCPS_CLOSED, 0);
                    break;
                case TCPS_SYN_SENT:
                case TCPS_LISTEN: 
                    setState(TCPS_CLOSED);
                    break;
                case TCPS_CLOSE_WAIT:
                    sendFIN();
                    setState(TCPS_LAST_ACK);
                    waitUntilState(TCPS_CLOSED, 0);
                    break;
                case TCPS_CLOSED: 
                    // Ignore
                    break;
                default:
                    throw new SocketException("Illegal state in close (" + getStateName() + ")");
            }
        } catch (TimeoutException ex) {
            throw (SocketException) new SocketException("Timeout").initCause(ex);
        }
        if (isReset()) {
            throw new SocketException("Connection reset");
        }
    }

    /**
     * Send data to the foreign side. This method can split-up the data in
     * chunks and blocks until there is space in the send buffer to hold the
     * data.
     * 
     * @param data
     * @param offset
     * @param length
     * @throws SocketException
     */
    public void appSendData(byte[] data, int offset, int length) throws SocketException {
        log.debug("appSendData(data, " + offset + ", " + length + ")");
        if (!isState(TCPS_ESTABLISHED) && !isState(TCPS_CLOSE_WAIT)) {
            throw new SocketException("Illegal state to send data: " + getStateName());
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset " + offset);
        }
        if (length < 0) {
            throw new IllegalArgumentException("length " + length);
        }
        final int mss = outChannel.getMss();
        while (length > 0) {
            final int chunk = Math.min(length, mss);
            // Create the TCP header
            final TCPHeader hdr = createOutgoingTCPHeader(TCPF_ACK, inChannel.getRcvNext());
            // Create the IP header
            final IPv4Header ipHdr = createOutgoingIPv4Header();
            // Send the chunk of data
            outChannel.send(ipHdr, hdr, data, offset, chunk);
            // Update length & offset
            offset += chunk;
            length -= chunk;
        }
    }

    /**
     * Return the number of available bytes in the input buffer.
     */
    public int appAvailable() {
        return inChannel.available();
    }

    /**
     * Read data from the input buffer up to len bytes long. Block until there
     * is data available.
     * 
     * @param dst
     * @param off
     * @param len
     * @return The number of bytes read
     */
    public int appRead(byte[] dst, int off, int len) throws SocketException {
        return inChannel.read(dst, off, len);
    }

    /**
     * @return Returns the state.
     */
    public final int getState() {
        return this.curState;
    }

    /**
     * @return Returns the name of the current state.
     */
    public final String getStateName() {
        return TCP_STATE_NAMES[this.curState];
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + ", state " + TCP_STATE_NAMES[curState];
    }

    /**
     * Has this connection been reset
     * 
     * @return Returns the reset.
     */
    public final boolean isReset() {
        return this.reset;
    }

    /**
     * @return Returns the refused.
     */
    public final boolean isRefused() {
        return this.refused;
    }

    public int getReceiveBufferSize() {
        return inChannel.getBufferSize();
    }

    public int getSendBufferSize() {
        return outChannel.getBufferSize();
    }
}
