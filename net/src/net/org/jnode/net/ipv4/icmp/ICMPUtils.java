/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import java.net.SocketException;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Service;

/**
 * Utility class for other IP protocols, to allow them to call simple methods
 * for generating ICMP messages.
 * 
 * @author epr
 */
public class ICMPUtils {
	
	private final IPv4Service ipService;
	//private ICMPProtocol icmp;
	
	/**
	 * Create a new instance
	 * @param ipService
	 */
	public ICMPUtils(IPv4Service ipService) {
		this.ipService = ipService;
	}
	
	/**
	 * Send an ICMP port unreachable message in response to the given
	 * buffer. 
	 * The IP header and the Transport layer header must be set as
	 * attributes in srcBuf, and must be removed from the head of the buffer.
	 * @param srcBuf
	 */
	public void sendPortUnreachable(SocketBuffer srcBuf) 
	throws SocketException {
		sendUnreachable(srcBuf, 3);
	}

	/**
	 * Send an ICMP unreachable message in response to the given
	 * buffer. 
	 * The IP header and the Transport layer header must be set as
	 * attributes in srcBuf, and must be removed from the head of the buffer.
	 * @param srcBuf
	 */
	private final void sendUnreachable(SocketBuffer srcBuf, int code) 
	throws SocketException {
		// Do not respond to linklayer broadcast messages
		if (srcBuf.getLinkLayerHeader().getDestinationAddress().isBroadcast()) {
			return;
		}
		
		// Gets the original IP header
		final IPv4Header origIpHdr = (IPv4Header)srcBuf.getNetworkLayerHeader();
		
		// Do not respond to networklayer broadcast/multicast messages
		if (origIpHdr.getDestination().isBroadcast() ||
		    origIpHdr.getDestination().isMulticast()) {
			return;
		}		
		
		final int tos = 0;
		final int ttl = 0xFF;
		final IPv4Address dstAddr = origIpHdr.getSource();

		// Build the response ICMP header
		final ICMPHeader icmpHdr = new ICMPUnreachableHeader(code);
		// Build the response IP header		
		final IPv4Header ipHdr = new IPv4Header(tos, ttl, IPv4Constants.IPPROTO_ICMP, dstAddr, 0);
		
		// Unpull the original transportlayer header
		srcBuf.unpull(srcBuf.getTransportLayerHeader().getLength());
		
		// Unpull the original IP header
		srcBuf.unpull(origIpHdr.getLength());
		
		// Create a response buffer
		final SocketBuffer skbuf = new SocketBuffer();
		// Prefix the ICMP header to the response buffer
		icmpHdr.prefixTo(skbuf);
		// Append the original message to the response buffer
		skbuf.append(srcBuf);
		/// RFC says return as much as we can without exceeding 576 bytes.
		skbuf.trim(576);
		
		ipService.transmit(ipHdr, skbuf);
	}

	/**
	 * Gets the ICMP protocol handler
	 * @return
	 * @throws NoSuchProtocolException
	 */
	/*private ICMPProtocol getICMP() 
	throws NoSuchProtocolException {
		if (icmp == null) {
			icmp = (ICMPProtocol)ipService.getProtocol(IPv4Constants.IPPROTO_ICMP);
		}
		return icmp;
	}*/
}
