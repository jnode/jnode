/*
 * $Id$
 */
package org.jnode.net.ipv4.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ExSocketOptions;
import java.net.InetAddress;
import java.net.SocketException;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.util.AbstractDatagramSocketImpl;

/**
 * @author epr
 */
public class UDPDatagramSocketImpl extends AbstractDatagramSocketImpl implements IPv4Constants, UDPConstants, ExSocketOptions {

	/** The UDP protocol we're using */
	private final UDPProtocol protocol;
	
	/**
	 * Create a new instance
	 * @param protocol
	 */
	public UDPDatagramSocketImpl(UDPProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * @see java.net.DatagramSocketImpl#bind(int, java.net.InetAddress)
	 */
	protected void doBind(int lport, InetAddress laddr) 
	throws SocketException {
		//Syslog.debug("doBind(" + lport + "," + laddr + ")");
		protocol.bind(this);
	}

	/**
	 * @see java.net.DatagramSocketImpl#close()
	 */
	protected void doClose() {
		protocol.unbind(this);
	}

	/**
	 * @see java.net.DatagramSocketImpl#receive(java.net.DatagramPacket)
	 */
	protected void onReceive(DatagramPacket p, SocketBuffer skbuf) 
	throws IOException {		
		final IPv4Header ipHdr = (IPv4Header)skbuf.getNetworkLayerHeader();
		final UDPHeader udpHdr = (UDPHeader)skbuf.getTransportLayerHeader();
		p.setData(skbuf.toByteArray(), 0, skbuf.getSize());
		p.setAddress(ipHdr.getSource().toInetAddress());
		p.setPort(udpHdr.getSrcPort());
		//Syslog.debug("UDP-onReceive: " + p);
	}
	
	/**
	 * @see java.net.DatagramSocketImpl#send(java.net.DatagramPacket)
	 */
	protected void send(DatagramPacket p) throws IOException {
		//Syslog.debug("UDP-send: " + p);
		
		final IPv4Address dstAddress = new IPv4Address(p.getAddress());
		final IPv4Header ipHdr;
		ipHdr = new IPv4Header(getTos(), getTimeToLive(), IPPROTO_UDP, dstAddress, p.getLength() + UDP_HLEN);
		if (!getLocalAddress().isAnyLocalAddress() || (getDevice() != null)) {
			ipHdr.setSource(new IPv4Address(getLocalAddress()));
		}
		final UDPHeader udpHdr;
		final int srcPort = p.getPort(); // or getLocalPort???? TODO Fix srcPort issue
		udpHdr = new UDPHeader(srcPort, p.getPort(), p.getLength());
		
		final SocketBuffer skbuf = new SocketBuffer(p.getData(), p.getOffset(), p.getLength());
		skbuf.setDevice(getDevice());		
		protocol.send(ipHdr, udpHdr, skbuf);
	}
}
