/*
 * $Id$
 */
package org.jnode.net.ipv4;

import java.net.SocketException;

import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;

/**
 * Interface for protocols within IP (like TCP, ICMP) 
 * @author epr
 */
public interface IPv4Protocol extends TransportLayer {

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
	throws SocketException;
}
