/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import java.net.DatagramPacket;

import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class BOOTPHeader {
	
	private final int opcode;
	private final int hwType;
	private final int hopCount;
	private final int transactionID;
	private final HardwareAddress clientHwAddress;
	private final IPv4Address clientIPAddress;
	private final IPv4Address yourIPAddress;
	private final IPv4Address serverIPAddress;
	private final IPv4Address gatewayIPAddress;
	
	/**
	 * Create a new header and read it from the given buffer
	 * @param skbuf
	 */
	public BOOTPHeader(SocketBuffer skbuf) {
		this.opcode = skbuf.get(0);
		this.hwType = skbuf.get(1);
		this.hopCount = skbuf.get(3);
		this.transactionID = skbuf.get32(4);
		this.clientIPAddress = new IPv4Address(skbuf, 12);
		this.yourIPAddress = new IPv4Address(skbuf, 16);
		this.serverIPAddress = new IPv4Address(skbuf, 20);
		this.gatewayIPAddress = new IPv4Address(skbuf, 24);
		if (hwType == 1) {
			clientHwAddress = new EthernetAddress(skbuf, 28);
		} else {
			clientHwAddress = null;
		}
	}

	/**
	 * Create a new header and read it from the given packet
	 * @param packet
	 */	
	public BOOTPHeader(DatagramPacket packet) {
		this(new SocketBuffer(packet.getData(), packet.getOffset(), packet.getLength()));
	}
	
	/**
	 * Create a new header
	 * @param opcode
	 * @param transactionID
	 * @param clientIPAddress
	 * @param clientHwAddress
	 */
	public BOOTPHeader(int opcode, int transactionID, IPv4Address clientIPAddress, HardwareAddress clientHwAddress) {
		this.opcode = opcode;
		this.hwType = clientHwAddress.getType();
		this.hopCount = 0;
		this.transactionID = transactionID;
		this.clientIPAddress = clientIPAddress;
		this.yourIPAddress = null;
		this.serverIPAddress = null;
		this.gatewayIPAddress = null;
		this.clientHwAddress = clientHwAddress;
	}
	
	/**
	 * Prefix this header to the given buffer
	 * @param skbuf
	 */
	public void prefixTo(SocketBuffer skbuf) {
		skbuf.insert(300);
		skbuf.set(0, opcode);
		skbuf.set(1, hwType);
		skbuf.set(2, clientHwAddress.getLength());
		skbuf.set(3, hopCount);
		skbuf.set32(4, transactionID);
		if (clientIPAddress != null) {
			clientIPAddress.writeTo(skbuf, 12);
		}
		if (yourIPAddress != null) {
			yourIPAddress.writeTo(skbuf, 16);
		}
		if (serverIPAddress != null) {
			serverIPAddress.writeTo(skbuf, 20);
		}
		if (gatewayIPAddress != null) {
			gatewayIPAddress.writeTo(skbuf, 24);
		}
		if (clientHwAddress != null) {
			clientHwAddress.writeTo(skbuf, 28);
		}
	}
	
	/**
	 * Gets this header as DatagramPacket
	 */
	public DatagramPacket asDatagramPacket() {
		final SocketBuffer skbuf = new SocketBuffer();
		prefixTo(skbuf);
		final byte[] data = skbuf.toByteArray();
		return new DatagramPacket(data, data.length);
	}

	/**
	 * Gets the client hardware address
	 */
	public HardwareAddress getClientHwAddress() {
		return clientHwAddress;
	}

	/**
	 * Gets the client IP address
	 */
	public IPv4Address getClientIPAddress() {
		return clientIPAddress;
	}

	/**
	 * Gets the gateway IP address
	 */
	public IPv4Address getGatewayIPAddress() {
		return gatewayIPAddress;
	}

	/**
	 * Gets the hop count
	 */
	public int getHopCount() {
		return hopCount;
	}

	/**
	 * Gets the hardware type
	 */
	public int getHwType() {
		return hwType;
	}

	/**
	 * Gets the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * Gets the server IP address
	 */
	public IPv4Address getServerIPAddress() {
		return serverIPAddress;
	}

	/**
	 * Gets the transaction ID
	 */
	public int getTransactionID() {
		return transactionID;
	}

	/**
	 * Gets your IP address
	 */
	public IPv4Address getYourIPAddress() {
		return yourIPAddress;
	}

}
