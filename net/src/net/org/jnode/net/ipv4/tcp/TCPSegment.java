/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import org.jnode.net.ipv4.IPv4Header;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPSegment {

	protected final IPv4Header ipHdr;
	protected final TCPHeader hdr;
	
	
	/**
	 * @param ipHdr
	 * @param hdr
	 */
	public TCPSegment(IPv4Header ipHdr, TCPHeader hdr) {
		this.ipHdr = ipHdr;
		this.hdr = hdr;
	}

	public final int getSeqNr() {
		return hdr.getSequenceNr();
	}
}
