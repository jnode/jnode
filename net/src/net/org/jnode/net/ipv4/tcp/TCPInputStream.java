/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPInputStream extends InputStream {

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
	public TCPInputStream(TCPControlBlock controlBlock, TCPSocketImpl impl) {
		this.controlBlock = controlBlock;
		this.impl = impl;
		this.closed = false;
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	public final int available() throws IOException {
		if (closed) {
			return 0;
		} else {
			return controlBlock.appAvailable();
		}
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public final void close() throws IOException {
		if (!this.closed) {
			this.closed = true;
			// Close the socket itself
			impl.close();
		}
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public final int read() throws IOException {
		final byte[] buf = new byte[1];
		if (read(buf, 0, 1) == 1) {
			return buf[0] & 0xff;
		} else {
			return -1;
		}
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public final int read(byte[] b, int off, int len) throws IOException {
		if (closed) {
			return -1;
		} else {
			return controlBlock.appRead(b, off, len);
		}
	}

	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	public final int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

}
