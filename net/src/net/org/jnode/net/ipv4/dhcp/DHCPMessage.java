/*
 * $Id$
 */
package org.jnode.net.ipv4.dhcp;

import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.bootp.BOOTPHeader;

/**
 * RFC 2131 and 2132.
 * @author markhale
 */
public class DHCPMessage {

	public static final int OPTIONS_SIZE = 312;
	public static final int SIZE = BOOTPHeader.SIZE + OPTIONS_SIZE;

	// RFC 2132
	public static final int PAD_OPTION = 0;
	public static final int SUBNET_MASK_OPTION = 1;
	public static final int TIME_OFFSET_OPTION = 2;
	public static final int ROUTER_OPTION = 3;
	public static final int DOMAIN_NAME_OPTION = 15;
	public static final int REQUESTED_IP_ADDRESS_OPTION = 50;
	public static final int MESSAGE_TYPE_OPTION = 53;
	public static final int SERVER_IDENTIFIER_OPTION = 54;
	public static final int MESSAGE_OPTION = 56;
	public static final int RENEWAL_TIME_OPTION = 58;
	public static final int REBINDING_TIME_OPTION = 59;
	public static final int CLIENT_IDENTIFIER_OPTION = 61;
	public static final int TFTP_SERVER_OPTION = 66;
	public static final int SMTP_SERVER_OPTION = 69;
	public static final int POP3_SERVER_OPTION = 70;
	public static final int NNTP_SERVER_OPTION = 71;
	public static final int END_OPTION = 255;

	// message types
	public static final int DHCPDISCOVER = 1;
	public static final int DHCPOFFER = 2;
	public static final int DHCPREQUEST = 3;
	public static final int DHCPDECLINE = 4;
	public static final int DHCPACK = 5;
	public static final int DHCPNAK = 6;
	public static final int DHCPRELEASE = 7;
	public static final int DHCPINFORM = 8;

	private final BOOTPHeader header;
	private int messageType;
	private final Map options = new HashMap();

	/**
	 * Create a new message
	 */
	private DHCPMessage(BOOTPHeader hdr) {
		header = hdr;
	}
	public DHCPMessage(BOOTPHeader hdr, int msgType) {
		this(hdr);
		messageType = msgType;
	}
	public DHCPMessage(SocketBuffer skbuf) {
		this(new BOOTPHeader(skbuf));
		int i = BOOTPHeader.SIZE + 4;
		int optionCode = skbuf.get(i);
		while(optionCode != END_OPTION) {
			if(optionCode == PAD_OPTION) {
				i++;
			} else {
				int optionLength = skbuf.get(i+1);
				byte[] optionValue = new byte[optionLength];
				skbuf.get(optionValue, 0, i+2, optionLength);
				setOption(optionCode, optionValue);
				i += optionLength + 2;
			}
			optionCode = skbuf.get(i);
		}
	}
	public DHCPMessage(DatagramPacket packet) {
		this(new SocketBuffer(packet.getData(), packet.getOffset(), packet.getLength()));
	}

	public BOOTPHeader getHeader() {
		return header;
	}

	public void setMessageType(int value) {
		messageType = value;
	}
	public int getMessageType() {
		return messageType;
	}

	public void setOption(int code, byte[] value) {
		if(code == MESSAGE_TYPE_OPTION)
			messageType = value[0];
		else
			options.put(new Integer(code), value);
	}
	public byte[] getOption(int code) {
		if(code == MESSAGE_TYPE_OPTION)
			return new byte[] {(byte) messageType};
		else
			return (byte[]) options.get(new Integer(code));
	}

	/**
	 * Gets this message as a DatagramPacket
	 */
	public DatagramPacket toDatagramPacket() {
		final SocketBuffer skbuf = new SocketBuffer();
		skbuf.insert(OPTIONS_SIZE);
		// magic cookie
		skbuf.set(0, 99);
		skbuf.set(1, 130);
		skbuf.set(2, 83);
		skbuf.set(3, 99);
		// options
		skbuf.set(4, MESSAGE_TYPE_OPTION);
		skbuf.set(5, 1);
		skbuf.set(6, messageType);
		int n = 7;
		Iterator iter = options.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			int optionCode = ((Integer)entry.getKey()).intValue();
			byte optionValue[] = (byte[]) entry.getValue();
			skbuf.set(n, optionCode);
			skbuf.set(n+1, optionValue.length);
			skbuf.set(n+2, optionValue, 0, optionValue.length);
			n += optionValue.length+2;
		}
		skbuf.set(n, END_OPTION);

		header.prefixTo(skbuf);
		final byte[] data = skbuf.toByteArray();
		return new DatagramPacket(data, data.length);
	}
}
