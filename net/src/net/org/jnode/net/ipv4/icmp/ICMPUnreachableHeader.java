/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPUnreachableHeader extends ICMPHeader {

	/**
	 * @param code
	 */
	public ICMPUnreachableHeader(int code) {
		super(ICMP_DEST_UNREACH, code);
	}

	/**
	 * @param skbuf
	 */
	public ICMPUnreachableHeader(SocketBuffer skbuf) {
		super(skbuf);
		final int type = getType();
		if (type != ICMP_DEST_UNREACH) {
			throw new IllegalArgumentException("Invalid type " + type);
		}
	}

	/**
	 * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
	 */
	protected void doPrefixTo(SocketBuffer skbuf) {
		skbuf.set16(4, 0); // Unused, must be 0
	}

	/**
	 * @see org.jnode.net.LayerHeader#getLength()
	 */
	public int getLength() {
		return 8;
	}

}
