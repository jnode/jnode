/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPTimestampHeader extends ICMPExHeader {

	private final int originateTimestamp;
	private final int receiveTimestamp;
	private final int transmitTimestamp;

	/**
	 * @param type
	 * @param identifier
	 * @param seqNumber
	 * @param originateTimestamp
	 * @param receiveTimestamp
	 * @param transmitTimestamp
	 */
	public ICMPTimestampHeader(
		int type,
		int identifier,
		int seqNumber,
		int originateTimestamp,
		int receiveTimestamp,
		int transmitTimestamp) {
		super(type, 0, identifier, seqNumber);
		if ((type != ICMP_TIMESTAMP) && (type != ICMP_TIMESTAMPREPLY)) {
			throw new IllegalArgumentException("Invalid type " + type);
		}
		this.originateTimestamp = originateTimestamp;
		this.receiveTimestamp = receiveTimestamp;
		this.transmitTimestamp = transmitTimestamp;
	}

	/**
	 * @param skbuf
	 */
	public ICMPTimestampHeader(SocketBuffer skbuf) {
		super(skbuf);
		final int type = getType();
		if ((type != ICMP_TIMESTAMP) && (type != ICMP_TIMESTAMPREPLY)) {
			throw new IllegalArgumentException("Invalid type " + type);
		}
		this.originateTimestamp = skbuf.get32(8);
		this.receiveTimestamp = skbuf.get32(12);
		this.transmitTimestamp = skbuf.get32(16);
	}

	/**
	 * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
	 */
	protected void doPrefixTo(SocketBuffer skbuf) {
		super.doPrefixTo(skbuf);
		skbuf.set32(8, originateTimestamp);
		skbuf.set32(12, receiveTimestamp);
		skbuf.set32(16, transmitTimestamp);
	}

	/**
	 * @see org.jnode.net.LayerHeader#getLength()
	 */
	public int getLength() {
		return 20;
	}

	/**
	 * Gets the originate timestamp
	 */
	public int getOriginateTimestamp() {
		return originateTimestamp;
	}

	/**
	 * Gets the receive timestamp
	 */
	public int getReceiveTimestamp() {
		return receiveTimestamp;
	}

	/**
	 * Gets the transmit timestamp
	 */
	public int getTransmitTimestamp() {
		return transmitTimestamp;
	}

}
