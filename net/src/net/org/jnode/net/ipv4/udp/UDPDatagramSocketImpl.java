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
 
package org.jnode.net.ipv4.udp;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.util.AbstractDatagramSocketImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ExSocketOptions;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author epr
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class UDPDatagramSocketImpl extends AbstractDatagramSocketImpl implements IPv4Constants,
        UDPConstants, ExSocketOptions {
    /**
     * The UDP protocol we're using
     */
    private final UDPProtocol protocol;

    /**
     * Create a new instance
     * 
     * @param protocol
     */
    public UDPDatagramSocketImpl(UDPProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @see java.net.DatagramSocketImpl#bind(int, java.net.InetAddress)
     */
    protected void doBind(int lport, InetAddress laddr) throws SocketException {
        protocol.bind(this);
    }

    /**
     * @see java.net.DatagramSocketImpl#close()
     */
    protected void doClose() {
        protocol.unbind(this);
    }

    /**
     * @see java.net.DatagramSocketImpl#getTTL()
     */
    @SuppressWarnings("deprecation")
    protected byte getTTL() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.net.DatagramSocketImpl#receive(java.net.DatagramPacket)
     */
    protected void onReceive(DatagramPacket p, SocketBuffer skbuf) throws IOException {
        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        final UDPHeader udpHdr = (UDPHeader) skbuf.getTransportLayerHeader();
        p.setData(skbuf.toByteArray(), 0, skbuf.getSize());
        p.setAddress(ipHdr.getSource().toInetAddress());
        p.setPort(udpHdr.getSrcPort());
    }

    /**
     * @see java.net.DatagramSocketImpl#send(java.net.DatagramPacket)
     */
    protected void send(DatagramPacket p) throws IOException {

        final IPv4Address dstAddress = new IPv4Address(p.getAddress());
        final IPv4Header ipHdr;
        ipHdr = new IPv4Header(getTos(), getTimeToLive(), IPPROTO_UDP, dstAddress, p.getLength() + UDP_HLEN);
        if (!getLocalAddress().isAnyLocalAddress() || (getDevice() != null)) {
            ipHdr.setSource(new IPv4Address(getLocalAddress()));
        }
        final UDPHeader udpHdr;
        final int srcPort = getLocalPort();
        // final int srcPort = p.getPort(); // or getLocalPort???? TODO Fix
        // srcPort issue
        udpHdr = new UDPHeader(srcPort, p.getPort(), p.getLength());

        final SocketBuffer skbuf = new SocketBuffer(p.getData(), p.getOffset(), p.getLength());
        skbuf.setDevice(getDevice());
        protocol.send(ipHdr, udpHdr, skbuf);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * @see java.net.DatagramSocketImpl#setTTL(byte)
     */
    @SuppressWarnings("deprecation")
    protected void setTTL(byte ttl) throws IOException {
        // TODO Auto-generated method stub

    }
}
