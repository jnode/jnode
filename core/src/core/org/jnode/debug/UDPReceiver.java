/*
 * $Id$
 */
package org.jnode.debug;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPReceiver {

	private final DatagramSocket socket;
	private FileOutputStream out;
	
	public static void main(String[] args) 
	throws IOException {
		final int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5612;
		final String fname = (args.length > 1) ? args[1] : null;
		final SocketAddress address = new InetSocketAddress(port);
		new UDPReceiver(address, fname).run();
	}
	
	public UDPReceiver(SocketAddress address, String fname)
	throws IOException {
		this.socket = new DatagramSocket(address);
		this.out = (fname != null) ? new FileOutputStream(fname, true) : null;
	}
	
	public void run() 
	throws IOException {
		final byte[] buf = new byte[2*4096];
		final DatagramPacket p = new DatagramPacket(buf, buf.length);
		while (true) {
			socket.receive(p);
			System.out.write(p.getData(), p.getOffset(), p.getLength());
			if (out != null) {
				out.write(p.getData(), p.getOffset(), p.getLength());
				out.flush();
			}
		}
	}

}
