/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.net.ipv4.tcp;

import org.jnode.net.SocketBuffer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPDataBuffer {

    private final byte[] data;
    private final int length;
    private int used;

    /**
     * Create a new instance
     * 
     * @param length
     */
    public TCPDataBuffer(int length) {
        this.data = new byte[length];
        this.length = length;
        this.used = 0;
    }

    /**
     * Create a socketbuffer for a given portion of this databuffer.
     * 
     * @param offset
     * @param length
     * @return The created buffer
     */
    public SocketBuffer createSocketBuffer(int offset, int length) {
        if ((offset < 0) || (offset >= used)) {
            throw new IndexOutOfBoundsException("offset " + offset);
        }
        if ((length < 0) || (offset + length > used)) {
            throw new IndexOutOfBoundsException("offset + length" + offset + "+" + length);
        }
        return new SocketBuffer(data, offset, length);
    }

    /**
     * Remove the first count byte from this buffer
     * 
     * @param count
     */
    public void pull(int count) {
        if ((count < 0) || (count > used)) {
            // Avoid errors in SYN & FIN situations
            // TODO create a better solution for this
            if (count > 2) {
                throw new IllegalArgumentException("count " + count);
            }
        } else {
            used -= count;
            System.arraycopy(data, count, data, 0, used);
        }
    }

    /**
     * Add the given data to this buffer.
     * 
     * @param src
     * @param srcOffset
     * @param length
     * @return The offset of the added data within the buffer.
     */
    public int add(byte[] src, int srcOffset, int length) {
        if (length > getFreeSize()) {
            throw new IllegalArgumentException("Not enough free space");
        }
        final int dstOffset = this.used;
        System.arraycopy(src, srcOffset, this.data, dstOffset, length);
        this.used += length;
        return dstOffset;
    }

    /**
     * Add the given data to this buffer.
     * 
     * @return The offset of the added data within the buffer.
     */
    public int add(SocketBuffer skbuf, int index, int length) {
        if (length > getFreeSize()) {
            throw new IllegalArgumentException("Not enough free space");
        }
        final int dstOffset = this.used;
        skbuf.get(this.data, dstOffset, index, length);
        this.used += length;
        return dstOffset;
    }

    /**
     * @return Returns the length.
     */
    public final int getLength() {
        return this.length;
    }

    /**
     * @return Returns the used.
     */
    public final int getUsed() {
        return this.used;
    }

    /**
     * Gets the number of free bytes in this buffer
     */
    public final int getFreeSize() {
        return length - used;
    }

    /**
     * Read data from the start of this buffer and remove it.
     * 
     * @param b
     * @param off
     * @param len
     * @return The number of bytes read
     */
    public int read(byte[] b, int off, int len) {
        len = Math.min(used, len);
        System.arraycopy(data, 0, b, off, len);
        pull(len);
        return len;
    }
}
