/*
 * $Id$
 */
package org.jnode.net.ipv4.raw;

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class RAWProtocol implements IPv4Protocol, IPv4Constants {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** The service i'm a part of */
	//private final IPv4Service ipService;
	/** My statistics */
	private final RAWStatistics stat = new RAWStatistics();

	/**
	 * Create a new instance
	 * @param ipService
	 */
	public RAWProtocol(IPv4Service ipService) {
		//this.ipService = ipService;
	}

	/**
	 * @see org.jnode.net.ipv4.IPv4Protocol#getName()
	 */
	public String getName() {
		return "raw";
	}

	/**
	 * @see org.jnode.net.ipv4.IPv4Protocol#getProtocolID()
	 */
	public int getProtocolID() {
		return IPPROTO_RAW;
	}

	/**
	 * @see org.jnode.net.ipv4.IPv4Protocol#receive(org.jnode.net.SocketBuffer)
	 */
	public void receive(SocketBuffer skbuf) throws SocketException {
		log.debug("Received RAW IP packet");
		// TODO Implement RAW protocol reception

	}

	/**
	 * Process an ICMP error message that has been received and matches
	 * this protocol. 
	 * The skbuf is position directly after the ICMP header (thus contains
	 * the error IP header and error transport layer header).
	 * The transportLayerHeader property of skbuf is set to the 
	 * ICMP message header.
	 * 
	 * @param skbuf
	 * @throws SocketException
	 */
	public void receiveError(SocketBuffer skbuf)
	throws SocketException {
		// Ignore errors here
	}
	
	/**
	 * Gets the SocketImplFactory of this protocol.
	 * @throws SocketException If this protocol is not Socket based.
	 */
	public SocketImplFactory getSocketImplFactory()
	throws SocketException {
		throw new SocketException("RAW is packet based");
	}

	/**
	 * Gets the DatagramSocketImplFactory of this protocol.
	 * @throws SocketException If this protocol is not DatagramSocket based.
	 */
	public DatagramSocketImplFactory getDatagramSocketImplFactory()
	throws SocketException {
		throw new SocketException("Not implemented yet");
	}

	/**
	 * @see org.jnode.net.ipv4.IPv4Protocol#getStatistics()
	 */
	public Statistics getStatistics() {
		return stat;
	}

}
