/*
 * $Id$
 */
package org.jnode.net.ipv4;

import java.net.SocketException;

import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public interface IPv4Service {
	
	/**
	 * Gets the routing table
	 */
	public IPv4RoutingTable getRoutingTable();
	
	/**
	 * Transmit an IP packet.
	 * The given buffer must contain all packet data AND the header(s)
	 * of any IP sub-protocols, before this method is called.
	 * 
	 * The following fields of the IP header must be set:
	 * tos, ttl, protocol, dstAddress.
	 * <p/>
	 * All other header fields are set, unless they have been set before.
	 * <p/>
	 * The following fields are always set (also when set before):
	 * version, hdrlength, identification, fragmentOffset, checksum
	 * <p/>
	 * If the device attribute of the skbuf has been set, the packet will
	 * be send to this device, otherwise a suitable route will be searched
	 * for in the routing table.
	 * 
	 * @param hdr
	 * @param skbuf
	 * @throws NoRouteToHostException No suitable route for this packet was found
	 * @throws NetworkException The packet could not be transmitted.
	 */
	public void transmit(IPv4Header hdr, SocketBuffer skbuf)
	throws SocketException;

	/**
	 * Gets the protocol for a given ID
	 * @param protocolID
	 * @throws NoSuchProtocolException No protocol with the given ID was found.
	 */
	public IPv4Protocol getProtocol(int protocolID) 
	throws NoSuchProtocolException;
}
