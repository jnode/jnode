/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class ICMPAddressMaskHeader extends ICMPExHeader {

	private final IPv4Address subnetMask;

	/**
	 * @param type
	 */
	public ICMPAddressMaskHeader(int type, int identifier, int seqNumber, IPv4Address subnetMask) {
		super(type, 0, identifier, seqNumber);
		if ((type != ICMP_ADDRESS) && (type != ICMP_ADDRESSREPLY)) {
			throw new IllegalArgumentException("Invalid type " + type);
		}
		this.subnetMask = subnetMask;
	}

	/**
	 * @param skbuf
	 */
	public ICMPAddressMaskHeader(SocketBuffer skbuf) {
		super(skbuf);
		final int type = getType();
		if ((type != ICMP_ADDRESS) && (type != ICMP_ADDRESSREPLY)) {
			throw new IllegalArgumentException("Invalid type " + type);
		}
		this.subnetMask = new IPv4Address(skbuf, 8);
	}

	/**
	 * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
	 */
	protected void doPrefixTo(SocketBuffer skbuf) {
		super.doPrefixTo(skbuf);
		subnetMask.writeTo(skbuf, 8);
	}

	/**
	 * @see org.jnode.net.LayerHeader#getLength()
	 */
	public int getLength() {
		return 12;
	}

	/**
	 * Gets the subnet mask
	 */
	public IPv4Address getSubnetMask() {
		return subnetMask;
	}

}
