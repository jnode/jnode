/*
 * $Id$
 */
package org.jnode.test.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author epr
 */
public class UDPTest {
	
	public static void main(String[] args) 
	throws Exception {
		
		// Listen to netbios messages
		final DatagramSocket socket = new DatagramSocket(2237);
		try {
			final byte[] buf = new byte[256];
			final DatagramPacket dp = new DatagramPacket(buf, buf.length);
		
			System.out.println("Starting to listen for netbios messages now...");
		
			for (int i = 0; i < 5; i++) {
				socket.receive(dp);
				System.out.println("Received datagram packet from " + dp.getAddress() + ":" + dp.getPort());
			}
		
			System.out.println("I'm stopping now");
		} finally {
			socket.close();
		}
	}

}
