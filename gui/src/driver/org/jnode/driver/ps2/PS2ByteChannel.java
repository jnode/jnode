/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

import org.jnode.util.ByteQueue;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class PS2ByteChannel implements ByteChannel {

	private final ByteQueue queue = new ByteQueue(1024);

	public PS2ByteChannel() {
		
	}

	/**
	 * @see org.jnode.system.IRQHandler#handleInterrupt(int)
	 */
	public void handleScancode(int b) {
		queue.push((byte) b);
	}

	/**
	 * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
	 */
	public int read(ByteBuffer dst) throws IOException {
		if (!isOpen()) {
			throw new ClosedChannelException();
		}

		// ToDo: proper exception handling (if end of queue -> IOException)
		int i;
		for (i = 0; i < dst.remaining(); i++) {
			dst.put(queue.pop());
		}
		return i;
	}

	/**
	 * Read a single byte from this channel.
	 * This method blocks until a byte is available.
	 */
	public int read(long timeout) throws IOException, TimeoutException, InterruptedException {
		if (!isOpen()) {
			throw new ClosedChannelException();
		}
		return queue.pop(timeout) & 0xFF;
	}

	/**
	 * Return the first byte from this channel, without removing it.
	 * This method blocks until a byte is available.
	 */
	public int peek(long timeout) throws IOException, TimeoutException, InterruptedException {
		if (!isOpen()) {
			throw new ClosedChannelException();
		}
		return queue.peek(timeout) & 0xFF;
	}

	/**
	 * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
	 */
	public int write(ByteBuffer src) throws IOException {
		throw new NonWritableChannelException();
	}

	/**
	 * @see java.nio.channels.Channel#close()
	 */
	public void close() throws IOException {
	}

	/**
	 * @see java.nio.channels.Channel#isOpen()
	 */
	public boolean isOpen() {
		return true;
	}
	
	/**
	 * Is this channel empty?
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	/**
	 * Remove all data from this channel
	 */
	public void clear() {
		while (!queue.isEmpty()) {
			queue.pop();
		}
	}

}
