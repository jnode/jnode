/*
 * $Id$
 */
package org.jnode.debug;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPReceiver {

	private final DatagramSocket socket;
	
	public static void main(String[] args) 
	throws IOException {
		final int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5612;
		final SocketAddress address = new InetSocketAddress(port);
		new UDPReceiver(address).run();
	}
	
	public UDPReceiver(SocketAddress address)
	throws SocketException {
		this.socket = new DatagramSocket(address);
	}
	
	public void run() 
	throws IOException {
		final byte[] buf = new byte[2*4096];
		final DatagramPacket p = new DatagramPacket(buf, buf.length);
		while (true) {
			socket.receive(p);
			System.out.write(p.getData(), p.getOffset(), p.getLength());
		}
	}

}
