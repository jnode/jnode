/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPEchoHeader extends ICMPExHeader {

	/**
	 * @param type
	 * @param identifier
	 * @param seqNumber
	 */
	public ICMPEchoHeader(int type, int identifier, int seqNumber) {
		super(type, 0, identifier, seqNumber);
	}

	/**
	 * @param skbuf
	 */
	public ICMPEchoHeader(SocketBuffer skbuf) {
		super(skbuf);
	}

	/**
	 * @see org.jnode.net.LayerHeader#getLength()
	 */
	public int getLength() {
		return 8;
	}

	/**
	 * Create a reply header based on info in this header
	 * @throws IllegalArgumentException If the type of this header is not equal to ICMP_ECHO.
	 * @return A header that is a suitable reply to this message
	 */
	public ICMPEchoHeader createReplyHeader() {
		if (getType() != ICMP_ECHO) {
			throw new IllegalArgumentException("Not an echo request");
		}
		return new ICMPEchoHeader(ICMP_ECHOREPLY, getIdentifier(), getSeqNumber());
	}
}
