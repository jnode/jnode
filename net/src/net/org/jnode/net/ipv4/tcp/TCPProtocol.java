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

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocketImplFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.apache.log4j.Logger;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.util.Statistics;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class TCPProtocol implements IPv4Protocol, IPv4Constants, TCPConstants {

    /** The IP service I'm a part of */
    private final IPv4Service ipService;

    /** The ICMP service */
    //private final ICMPUtils icmp;
    /** My statistics */
    private final TCPStatistics stat = new TCPStatistics();

    /** The SocketImpl factory for TCP */
    private final TCPSocketImplFactory socketImplFactory;

    /** My control blocks */
    private final TCPControlBlockList controlBlocks;

    /** The timer */
    private final TCPTimer timer;

    /** My logger */
    private static final Logger log = Logger.getLogger(TCPProtocol.class);

    /**
     * Initialize a new instance
     * 
     * @param ipService
     */
    public TCPProtocol(IPv4Service ipService) throws NetworkException {
        this.ipService = ipService;
        //this.icmp = new ICMPUtils(ipService);
        this.controlBlocks = new TCPControlBlockList(this);
        this.timer = new TCPTimer(controlBlocks);
        try {
            socketImplFactory = new TCPSocketImplFactory(this);
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                   public Object run() throws IOException {
                       Socket.setSocketImplFactory(socketImplFactory);
                       ServerSocket.setSocketFactory(socketImplFactory);
                       return null;
                   }
                });
            } catch (SecurityException ex) {
                log.error("No permission for set socket factory.", ex);
            } catch (PrivilegedActionException ex) {
                throw new NetworkException(ex.getException());
            }
        } catch (IOException ex) {
            throw new NetworkException(ex);
        }
        timer.start();
    }

    /**
     * @see org.jnode.net.TransportLayer#getDatagramSocketImplFactory()
     */
    public DatagramSocketImplFactory getDatagramSocketImplFactory()
            throws SocketException {
        throw new SocketException("TCP is socket based");
    }

    /**
     * @see org.jnode.net.TransportLayer#getName()
     */
    public String getName() {
        return "tcp";
    }

    /**
     * @see org.jnode.net.TransportLayer#getProtocolID()
     */
    public int getProtocolID() {
        return IPPROTO_TCP;
    }

    /**
     * @see org.jnode.net.TransportLayer#getSocketImplFactory()
     */
    public SocketImplFactory getSocketImplFactory() throws SocketException {
        return socketImplFactory;
    }

    /**
     * @see org.jnode.net.TransportLayer#getStatistics()
     */
    public Statistics getStatistics() {
        return stat;
    }

    /**
     * @see org.jnode.net.TransportLayer#receive(org.jnode.net.SocketBuffer)
     */
    public void receive(SocketBuffer skbuf) throws SocketException {

        // Increment stats
        stat.ipackets.inc();

        // Get the IP header
        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();

        // Read the TCP header
        final TCPHeader hdr = new TCPHeader(skbuf);

        // Set the TCP header in the buffer-field
        skbuf.setTransportLayerHeader(hdr);
        // Remove the TCP header from the head of the buffer
        skbuf.pull(hdr.getLength());
        // Trim the buffer up to the length in the TCP header
        skbuf.trim(hdr.getDataLength());

        if (!hdr.isChecksumOk()) {
            if (log.isDebugEnabled()) {
                log.debug("Receive: badsum: " + hdr);
            }
            stat.badsum.inc();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Receive: " + hdr);
            }

            // Find the corresponding control block
            final TCPControlBlock cb = (TCPControlBlock) controlBlocks.lookup(
                    ipHdr.getSource(), hdr.getSrcPort(),
                    ipHdr.getDestination(), hdr.getDstPort(), true);
            if (cb == null) {
              final boolean ack = hdr.isFlagAcknowledgeSet();
              final boolean rst = hdr.isFlagResetSet();

              stat.noport.inc();

              // Port unreachable
              if (ack && rst) {
                // the source is also unreachable
                log.debug("Dropping segment due to: connection refused as the source is also unreachable");
              }
             else {
                processPortUnreachable(ipHdr, hdr);
              }
            } else {
                // Let the cb handle the receive
                cb.receive(hdr, skbuf);
            }
        }
    }

    /**
     * @see org.jnode.net.ipv4.IPv4Protocol#receiveError(org.jnode.net.SocketBuffer)
     */
    public void receiveError(SocketBuffer skbuf) throws SocketException {
        // TODO Auto-generated method stub

    }

    /**
     * Process a segment whose destination port is unreachable
     * 
     * @param hdr
     */
    private void processPortUnreachable(IPv4Header ipHdr, TCPHeader hdr)
            throws SocketException {
        final TCPHeader replyHdr = new TCPHeader(hdr.getDstPort(), hdr
                .getSrcPort(), 0, 0, hdr.getSequenceNr() + 1, 0, 0);
        replyHdr.setFlags(TCPF_ACK | TCPF_RST);
        final IPv4Header replyIpHdr = new IPv4Header(ipHdr);
        replyIpHdr.swapAddresses();
        send(replyIpHdr, replyHdr, new SocketBuffer());
    }

    /**
     * Create a binding for a local address
     * 
     * @param lAddr
     * @param lPort
     */
    public TCPControlBlock bind(IPv4Address lAddr, int lPort)
            throws BindException {
        return (TCPControlBlock) controlBlocks.bind(lAddr, lPort);
    }

    /**
     * Send an TCP packet
     * 
     * @param skbuf
     */
    protected void send(IPv4Header ipHdr, TCPHeader tcpHdr, SocketBuffer skbuf)
            throws SocketException {
        if (log.isDebugEnabled()) {
            log.debug("send(ipHdr, " + tcpHdr + ")");
        }
        skbuf.setTransportLayerHeader(tcpHdr);
        tcpHdr.prefixTo(skbuf);
        ipHdr.setDataLength(skbuf.getSize());
        ipService.transmit(ipHdr, skbuf);
        stat.opackets.inc();
    }

    /**
     * Get the current time counter
     */
    protected long getTimeCounter() {
        return timer.getCounter();
    }
}
