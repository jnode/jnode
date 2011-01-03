/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
     * @see org.jnode.system.resource.IRQHandler#handleInterrupt(int)
     */
    public void handleScancode(int b) {
        queue.enQueue((byte) b);
    }

    /**
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer dst) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        // FIXME: proper exception handling (if end of queue -> IOException)
        int i;
        for (i = 0; i < dst.remaining(); i++) {
            dst.put(queue.deQueue());
        }
        return i;
    }

    /**
     * Read a single byte from this channel. This method blocks until a byte is
     * available.
     */
    public int read(long timeout) throws IOException, TimeoutException, InterruptedException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        return queue.deQueue(timeout) & 0xFF;
    }

    /**
     * Return the first byte from this channel, without removing it. This method
     * blocks until a byte is available.
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
        queue.clear();
    }

}
