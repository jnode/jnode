/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import java.net.DatagramPacket;
import java.net.Inet4Address;

import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class BOOTPHeader {

	/** Size of the BOOTP header (236 bytes) */
	public static final int SIZE = 236;

	/** Client to server message */
	public static final int BOOTREQUEST = 1;
	/** Server to client message */
	public static final int BOOTREPLY = 2;

	private final int opcode;
	private final int hwType;
	private final int hopCount;
	private final int transactionID;
	private final int secondsElapsed;
	private final int flags;
	private final HardwareAddress clientHwAddress;
	private final Inet4Address clientIPAddress;
	private final Inet4Address yourIPAddress;
	private final Inet4Address serverIPAddress;
	private final Inet4Address gatewayIPAddress;
	
	/**
	 * Create a new header and read it from the given buffer
	 * @param skbuf
	 */
	public BOOTPHeader(SocketBuffer skbuf) {
		this.opcode = skbuf.get(0);
		this.hwType = skbuf.get(1);
		this.hopCount = skbuf.get(3);
		this.transactionID = skbuf.get32(4);
		this.secondsElapsed = skbuf.get16(8);
		this.flags = skbuf.get16(10);
		this.clientIPAddress = IPv4Address.readFrom(skbuf, 12);
		this.yourIPAddress = IPv4Address.readFrom(skbuf, 16);
		this.serverIPAddress = IPv4Address.readFrom(skbuf, 20);
		this.gatewayIPAddress = IPv4Address.readFrom(skbuf, 24);
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
	 * Create a new header (for client usage)
	 * @param opcode
	 * @param transactionID
	 * @param clientIPAddress
	 * @param clientHwAddress
	 */
	public BOOTPHeader(int opcode, int transactionID, Inet4Address clientIPAddress, HardwareAddress clientHwAddress) {
		this.opcode = opcode;
		this.hwType = clientHwAddress.getType();
		this.hopCount = 0;
		this.transactionID = transactionID;
		this.secondsElapsed = 0;
		this.flags = 0;
		this.clientIPAddress = clientIPAddress;
		this.yourIPAddress = null;
		this.serverIPAddress = null;
		this.gatewayIPAddress = null;
		this.clientHwAddress = clientHwAddress;
	}
	/**
	 * Create a new header (for server usage)
	 * @param opcode
	 * @param transactionID
	 * @param clientIPAddress
	 * @param yourIPAddress
	 * @param serverIPAddress
	 * @param clientHwAddress
	 */
	public BOOTPHeader(int opcode, int transactionID, Inet4Address clientIPAddress, Inet4Address yourIPAddress, Inet4Address serverIPAddress, HardwareAddress clientHwAddress) {
		this.opcode = opcode;
		this.hwType = clientHwAddress.getType();
		this.hopCount = 0;
		this.transactionID = transactionID;
		this.secondsElapsed = 0;
		this.flags = 0;
		this.clientIPAddress = clientIPAddress;
		this.yourIPAddress = yourIPAddress;
		this.serverIPAddress = serverIPAddress;
		this.gatewayIPAddress = null;
		this.clientHwAddress = clientHwAddress;
	}
	
	/**
	 * Prefix this header to the given buffer
	 * @param skbuf
	 */
	public void prefixTo(SocketBuffer skbuf) {
		skbuf.insert(SIZE);
		skbuf.set(0, opcode);
		skbuf.set(1, hwType);
		skbuf.set(2, clientHwAddress.getLength());
		skbuf.set(3, hopCount);
		skbuf.set32(4, transactionID);
		skbuf.set16(8, secondsElapsed);
		skbuf.set16(10, flags);
		if (clientIPAddress != null) {
			IPv4Address.writeTo(skbuf, 12, clientIPAddress);
		}
		if (yourIPAddress != null) {
			IPv4Address.writeTo(skbuf, 16, yourIPAddress);
		}
		if (serverIPAddress != null) {
			IPv4Address.writeTo(skbuf, 20, serverIPAddress);
		}
		if (gatewayIPAddress != null) {
			IPv4Address.writeTo(skbuf, 24, gatewayIPAddress);
		}
		if (clientHwAddress != null) {
			clientHwAddress.writeTo(skbuf, 28);
		}
	}

	/*private void setServerHostName(String sname) {
		final int len = sname.length();
		if(sname != null && len > 63)
			throw new IllegalArgumentException("Server host name is too long, "+len+" > 127.");
	}*/
	/*private void setBootFileName(String file) {
		final int len = file.length();
		if(file != null && len > 127)
			throw new IllegalArgumentException("Boot file name is too long, "+len+" > 127.");
	}*/

	/**
	 * Gets the client hardware address
	 */
	public HardwareAddress getClientHwAddress() {
		return clientHwAddress;
	}

	/**
	 * Gets the client IP address
	 */
	public Inet4Address getClientIPAddress() {
		return clientIPAddress;
	}

	/**
	 * Gets the gateway IP address
	 */
	public Inet4Address getGatewayIPAddress() {
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
	public Inet4Address getServerIPAddress() {
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
	public Inet4Address getYourIPAddress() {
		return yourIPAddress;
	}

}
