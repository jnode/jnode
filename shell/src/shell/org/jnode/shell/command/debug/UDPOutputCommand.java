/*
 * $Id$
 */
package org.jnode.shell.command.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
