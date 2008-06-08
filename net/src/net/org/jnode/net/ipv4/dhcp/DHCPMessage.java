/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.net.ipv4.dhcp;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.bootp.BOOTPHeader;

/**
 * RFC 2131 and 2132.
 * 
 * @author markhale
 */
public class DHCPMessage {

    public static final int OPTIONS_SIZE = 312;

    public static final int SIZE = BOOTPHeader.SIZE + OPTIONS_SIZE;

    // RFC 2132
    public static final int PAD_OPTION = 0;

    /** 4 bytes */
    public static final int SUBNET_MASK_OPTION = 1;

    /** Signed 32-bit integer, seconds */
    public static final int TIME_OFFSET_OPTION = 2;

    /** IP address */
    public static final int ROUTER_OPTION = 3;

    /** IP address */
    public static final int TIME_SERVER_OPTION = 4;

    /** IP address */
    public static final int NAME_SERVER_OPTION = 5;

    /** IP address */
    public static final int DNS_OPTION = 6;

    /** IP address */
    public static final int LOG_SERVER_OPTION = 7;

    /** IP address */
    public static final int COOKIE_SERVER_OPTION = 8;

    /** IP address */
    public static final int LPR_SERVER_OPTION = 9;

    /** String */
    public static final int HOST_NAME_OPTION = 12;

    /** String */
    public static final int DOMAIN_NAME_OPTION = 15;

    /** Byte */
    public static final int TTL_OPTION = 23;

    /** IP address */
    public static final int REQUESTED_IP_ADDRESS_OPTION = 50;

    /** Unsigned 32-bit integer, seconds */
    public static final int LEASE_TIME_OPTION = 51;

    /** Byte, FILE_OVERLOAD, SNAME_OVERLOAD, BOTH_OVERLOAD */
    public static final int OPTION_OVERLOAD_OPTION = 52;

    public static final int FILE_OVERLOAD = 1;

    public static final int SNAME_OVERLOAD = 2;

    public static final int BOTH_OVERLOAD = 3;

    /** Byte */
    public static final int MESSAGE_TYPE_OPTION = 53;

    /** IP address */
    public static final int SERVER_IDENTIFIER_OPTION = 54;

    /** String */
    public static final int MESSAGE_OPTION = 56;

    /** Unsigned 16-bit integer, minimum value is MIN_PACKET_SIZE */
    public static final int MAX_PACKET_SIZE_OPTION = 57;

    public static final int MIN_PACKET_SIZE = 576;

    /** Unsigned 32-bit integer, seconds */
    public static final int RENEWAL_TIME_OPTION = 58;

    /** Unsigned 32-bit integer, seconds */
    public static final int REBINDING_TIME_OPTION = 59;

    public static final int CLIENT_IDENTIFIER_OPTION = 61;

    public static final int TFTP_SERVER_OPTION = 66;

    /** IP address */
    public static final int SMTP_SERVER_OPTION = 69;

    /** IP address */
    public static final int POP3_SERVER_OPTION = 70;

    /** IP address */
    public static final int NNTP_SERVER_OPTION = 71;

    /** IP address */
    public static final int WWW_SERVER_OPTION = 72;

    /** IP address */
    public static final int FINGER_SERVER_OPTION = 73;

    /** IP address */
    public static final int IRC_SERVER_OPTION = 74;

    /** String URL: JNode specific option */
    public static final int PLUGIN_LOADER_OPTION = 130;

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

    private final Map<Integer, byte[]> options = new HashMap<Integer, byte[]>();

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
        while (optionCode != END_OPTION) {
            if (optionCode == PAD_OPTION) {
                i++;
            } else {
                int optionLength = skbuf.get(i + 1);
                byte[] optionValue = new byte[optionLength];
                skbuf.get(optionValue, 0, i + 2, optionLength);
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

    /**
     * Sets a DHCP option with an array of bytes.
     */
    public void setOption(int code, byte[] value) {
        if (code == MESSAGE_TYPE_OPTION) {
            messageType = value[0];
        } else {
            options.put(new Integer(code), value);
        }
    }

    /**
     * Sets a DHCP option with an unsigned 16-bit integer. Convenience method.
     */
    public void setOption16(int code, int value) {
        byte[] b = new byte[] {(byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF)};
        setOption(code, b);
    }

    /**
     * Sets a DHCP option with a string. Convenience method.
     */
    public void setOption(int code, String value) {
        try {
            setOption(code, value.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets a DHCP option with an IP address. Convenience method.
     */
    public void setOption(int code, Inet4Address value) {
        setOption(code, value.getAddress());
    }

    /**
     * Gets a DHCP option as an array of bytes.
     */
    public byte[] getOption(int code) {
        if (code == MESSAGE_TYPE_OPTION)
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
        for (Map.Entry<Integer, byte[]> entry : options.entrySet()) {
            final int optionCode = entry.getKey();
            final byte optionValue[] = entry.getValue();
            skbuf.set(n, optionCode);
            skbuf.set(n + 1, optionValue.length);
            skbuf.set(n + 2, optionValue, 0, optionValue.length);
            n += optionValue.length + 2;
        }
        skbuf.set(n, END_OPTION);

        header.prefixTo(skbuf);
        final byte[] data = skbuf.toByteArray();
        return new DatagramPacket(data, data.length);
    }
}
