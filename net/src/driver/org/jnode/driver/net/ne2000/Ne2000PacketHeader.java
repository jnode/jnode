/*
 * $Id$
 */
package org.jnode.driver.net.ne2000;

import org.jnode.util.NumberUtils;

/**
 * Represents a packet header of a Ne2000 PCI ringbuffer
 * @author epr
 */
public class Ne2000PacketHeader {
	
	private final int status;
	private final int nextPacketPage;
	private final int length;
	
	/**
	 * Create a new instance
	 * @param hdr
	 * @param hdrOffset
	 */
	public Ne2000PacketHeader(byte[] hdr, int hdrOffset) {
		status = hdr[hdrOffset+0] & 0xFF;
		nextPacketPage = hdr[hdrOffset+1] & 0xFF;
		length = ((hdr[hdrOffset+3] & 0xFF) << 8) | (hdr[hdrOffset+2] & 0xFF);
	}
	
	/**
	 * Gets the length of this packet
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Gets the page on which the next packet is or will be located,
	 */
	public int getNextPacketPage() {
		return nextPacketPage;
	}

	/**
	 * Gets the status of this packet
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "state:0x" + NumberUtils.hex(status, 2) + ", " +
			"next:0x" + NumberUtils.hex(nextPacketPage, 2) + ", " +
			"length:0x" + NumberUtils.hex(length, 4);
	}

}
