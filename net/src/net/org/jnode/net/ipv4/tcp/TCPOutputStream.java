/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPOutputStream extends OutputStream {

	/** The control block used to send data to */
	private final TCPControlBlock controlBlock;
	/** Has this stream been closed? */
	private boolean closed;
	/** The SocketImpl */
	private final TCPSocketImpl impl;

	/**
	 * Create a new instance
	 * 
	 * @param controlBlock
	 */
	public TCPOutputStream(TCPControlBlock controlBlock, TCPSocketImpl impl) {
		this.controlBlock = controlBlock;
		this.impl = impl;
		this.closed = false;
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (!this.closed) {
			this.closed = true;
			// Close the socket itself
			impl.close();
		}
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		super.flush();
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public final void write(byte[] b, int off, int len) throws IOException, NullPointerException, IndexOutOfBoundsException {
		if (closed) {
			throw new IOException("closed");
		} else {
			controlBlock.appSendData(b, off, len);
		}
	}

	/**
	 * @see java.io.OutputStream#write(byte[])
	 */
	public final void write(byte[] b) throws IOException, NullPointerException {
		write(b, 0, b.length);
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public final void write(int b) throws IOException {
		final byte[] buf = new byte[1];
		buf[0] = (byte) b;
		write(buf, 0, 1);
	}

}
