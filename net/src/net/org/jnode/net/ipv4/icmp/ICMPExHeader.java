/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * This class adds the identifier and sequencenumber fields to the header.
 * @author epr
 */
public abstract class ICMPExHeader extends ICMPHeader {

	private final int identifier;
	private final int seqNumber;

	/**
	 * @param type
	 */
	public ICMPExHeader(int type, int code, int identifier, int seqNumber) {
		super(type, code);
		this.identifier = identifier;
		this.seqNumber = seqNumber;
	}

	/**
	 * @param skbuf
	 */
	public ICMPExHeader(SocketBuffer skbuf) {
		super(skbuf);
		this.identifier = skbuf.get16(4);
		this.seqNumber = skbuf.get16(6);
	}

	/**
	 * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
	 */
	protected void doPrefixTo(SocketBuffer skbuf) {
		skbuf.set16(4, identifier);
		skbuf.set16(6, seqNumber);
	}

	/**
	 * Gets the identifier
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the sequence number
	 */
	public int getSeqNumber() {
		return seqNumber;
	}
}
