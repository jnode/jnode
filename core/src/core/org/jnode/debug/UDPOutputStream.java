/*
 * $Id$
 */
package org.jnode.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPOutputStream extends OutputStream {

	private final DatagramSocket socket;
	private final SocketAddress address;

	/**
	 * Create a new instance
	 * @param address
	 * @throws SocketException
	 */
	public UDPOutputStream(SocketAddress address) throws SocketException {
		socket = new DatagramSocket();
		this.address = address;
	}

	/**
	 * @see java.io.OutputStream#close()
	 * @throws IOException
	 */
	public void close() throws IOException {
		super.close();
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
	public void write(final byte[] b, final int off, final int len) throws IOException, NullPointerException, IndexOutOfBoundsException {
	    try {
	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
	        public Object run() throws IOException {
	    		final DatagramPacket p = new DatagramPacket(b, off, len);
	    		p.setSocketAddress(address);
	    		socket.send(p);
	    		return null;
	            }});
	    } catch (PrivilegedActionException ex) {
	        final IOException ioe = new IOException();
	        ioe.initCause(ex.getException());
	        throw ioe;
	    }
	}

	/**
	 * @param b
	 * @see java.io.OutputStream#write(byte[])
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public void write(byte[] b) throws IOException, NullPointerException {
		write(b, 0, b.length);
	}

	/**
	 * @param b
	 * @see java.io.OutputStream#write(int)
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		write(new byte[] {(byte) b }, 0, 1);
	}

}
