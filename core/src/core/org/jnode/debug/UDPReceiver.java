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
