/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import java.net.SocketException;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Header;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPOutSegment extends TCPSegment {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private final TCPDataBuffer buffer;
	private int dataOffset;
	/** Timeout counter, if 0, re-transmit */
	private int timeout;
	/** Number of timeout ticks (usually grows) */
	private int timeoutTicks;

	/**
	 * @param ipHdr
	 * @param hdr
	 * @param dataOffset
	 */
	public TCPOutSegment(IPv4Header ipHdr, TCPHeader hdr, TCPDataBuffer buffer, int dataOffset, int timeout) {
		super(ipHdr, hdr);
		this.buffer = buffer;
		this.dataOffset = dataOffset;
		this.timeout = timeout;
		this.timeoutTicks = timeout;
	}

	/**
	 * Process timeout handling
	 */
	public void timeout(TCPProtocol tcp)
	throws SocketException {
		timeout--;
		if (timeout == 0) {
			log.debug("Resend segment " + getSeqNr());
			send(tcp);
			timeoutTicks = timeoutTicks * 2;
			timeout = timeoutTicks;
		}
	}
	
	/**
	 * Send this segment
	 * 
	 * @param tcp
	 */
	public void send(TCPProtocol tcp) throws SocketException {
		final SocketBuffer skbuf;
		if (hdr.getDataLength() > 0) {
			skbuf = buffer.createSocketBuffer(dataOffset, hdr.getDataLength());
		} else {
			skbuf = new SocketBuffer(0);
		}
		tcp.send(ipHdr, hdr, skbuf);
	}

	/**
	 * @return Returns the dataOffset.
	 */
	public final int getDataOffset() {
		return this.dataOffset;
	}

	public final void adjustDataOffset(int diff) {
		if (hdr.getDataLength() > 0) {
			this.dataOffset -= diff;
		}
	}
	
	/**
	 * Does this segment only contain an ACK?
	 * @return True if this segment contains only an acknowledgement, false otherwise
	 */
	public boolean isAckOnly() {
		return ((hdr.getFlags() == TCPConstants.TCPF_ACK) && (hdr.getDataLength() == 0));
	}
}
