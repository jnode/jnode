/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import java.net.DatagramPacket;

import org.jnode.net.SocketBuffer;

/**
 * RFC 1542.
 * @author markhale
 */
public class BOOTPMessage {

	/** Size of the BOOTP vendor-specific area (64 bytes) */
	public static final int OPTIONS_SIZE = 64;
	/** Size of the BOOTP message (300 bytes) */
	public static final int SIZE = BOOTPHeader.SIZE + OPTIONS_SIZE;

	private final BOOTPHeader header;

	/**
	 * Create a new message
	 */
	public BOOTPMessage(BOOTPHeader hdr) {
		header = hdr;
	}

	public BOOTPMessage(SocketBuffer skbuf) {
		this(new BOOTPHeader(skbuf));
	}
	public BOOTPMessage(DatagramPacket packet) {
		this(new BOOTPHeader(new SocketBuffer(packet.getData(), packet.getOffset(), packet.getLength())));
	}

	public BOOTPHeader getHeader() {
		return header;
	}

	/**
	 * Gets this message as a DatagramPacket
	 */
	public DatagramPacket toDatagramPacket() {
		final SocketBuffer skbuf = new SocketBuffer();
		skbuf.insert(OPTIONS_SIZE);

		header.prefixTo(skbuf);
		final byte[] data = skbuf.toByteArray();
		return new DatagramPacket(data, data.length);
	}
}
