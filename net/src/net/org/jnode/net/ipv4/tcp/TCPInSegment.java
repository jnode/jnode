/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Header;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPInSegment extends TCPSegment {

	/** The data */
	protected final SocketBuffer skbuf;
	
	/**
	 * @param ipHdr
	 * @param hdr
	 */
	public TCPInSegment(IPv4Header ipHdr, TCPHeader hdr, SocketBuffer skbuf) {
		super(ipHdr, hdr);
		this.skbuf = skbuf;
	}

}
