/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell.command.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.jnode.debug.UDPAppender;
import org.jnode.debug.UDPOutputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPOutputCommand {

	public static void main(String[] args) 
	throws IOException {
		final int port = (args.length > 1) ? Integer.parseInt(args[1]) : 5612;
		final SocketAddress address = new InetSocketAddress(args[0], port);
		UDPOutputStream udpOut = new UDPOutputStream(address);
		DupOutputStream dupOut = new DupOutputStream(System.out, udpOut);
		PrintStream ps = new PrintStream(dupOut);
		System.setOut(ps);		
		System.setErr(ps);		
		
		final Logger root = Logger.getRootLogger();
		root.addAppender(new UDPAppender(udpOut, null));
		
	}
	
	static class DupOutputStream extends OutputStream {
		
		private final OutputStream os1;
		private final OutputStream os2;
		
		public DupOutputStream(OutputStream os1, OutputStream os2) {
			this.os1 = os1;
			this.os2 = os2;
		}
		
			/**
		 * @see java.io.OutputStream#close()
		 * @throws IOException
			 */
		public void close() throws IOException {
			os1.close();
			os2.close();
		}

		/**
		 * @see java.io.OutputStream#flush()
		 * @throws IOException
		 */
		public void flush() throws IOException {
			os1.flush();
			os2.flush();
		}

		/**
		 * @param b
		 * @param off
		 * @param len
		 * @see java.io.OutputStream#write(byte[], int, int)
		 * @throws IOException
		 * @throws NullPointerException
		 * @throws IndexOutOfBoundsException
		 */
		public void write(byte[] b, int off, int len) throws IOException, NullPointerException, IndexOutOfBoundsException {
			os1.write(b, off, len);
			os2.write(b, off, len);
		}

		/**
		 * @param b
		 * @see java.io.OutputStream#write(byte[])
		 * @throws IOException
		 * @throws NullPointerException
		 */
		public void write(byte[] b) throws IOException, NullPointerException {
			os1.write(b);
			os2.write(b);
		}

		/**
		 * @param b
		 * @see java.io.OutputStream#write(int)
		 * @throws IOException
		 */
		public void write(int b) throws IOException {
			os1.write(b);
			os2.write(b);
		}
	}
}
