/*
 * $Id$
 */
package org.jnode.test.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class UDPSendTest {

	public static void main(String[] args)
	throws Exception {
		
		final DatagramSocket socket = new DatagramSocket();
		try {
			final int size;
			if (args.length > 1) {
				size = Integer.parseInt(args[1]); 
			} else {
				size = 5000;
			}
		
			System.out.println("SendBufferSize=" + socket.getSendBufferSize());
			final byte[] buf = new byte[size];
			final DatagramPacket dp = new DatagramPacket(buf, buf.length);
		
			dp.setPort(2237);
			dp.setAddress(new IPv4Address(args[0]).toInetAddress());
		
			System.out.println("Sending packet of size " + dp.getLength());
			socket.send(dp);
		} finally {
			socket.close();
		}
	}
}
