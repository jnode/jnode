/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class DeviceAccess {

    /**
     * The number of bytes per character. ExFat uses UTF-16 everywhere, so this
     * equals 2.
     */
    public static final int BYTES_PER_CHAR = 2;

    private final BlockDeviceAPI dev;
    private final ByteBuffer buffer;

    public DeviceAccess(BlockDeviceAPI dev) {
        this.dev = dev;
        this.buffer = ByteBuffer.allocate(8);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public int getUint8(long offset) throws IOException {
        this.buffer.rewind();
        this.buffer.limit(1);
        this.dev.read(offset, buffer);
        this.buffer.rewind();

        return getUint8(this.buffer);
    }

    public long getUint32(long offset) throws IOException {
        this.buffer.rewind();
        this.buffer.limit(4);
        this.dev.read(offset, buffer);
        this.buffer.rewind();

        return getUint32(buffer);
    }

    public static int getUint8(ByteBuffer src) {
        return (src.get() & 0xff);
    }

    public static int getUint16(ByteBuffer src) {
        return (src.getShort() & 0xffff);
    }

    public static long getUint32(ByteBuffer src) {
        return (src.getInt() & 0xffffffffl);
    }

    public static long getUint64(ByteBuffer src) throws IOException {
        final long result = src.getLong();

        if (result < 0) {
            throw new IOException("value too big");
        }

        return result;
    }

    public static char getChar(ByteBuffer src) {
        return (char) src.getShort();
    }

    public char getChar(long offset) throws IOException {
        this.buffer.rewind();
        this.buffer.limit(BYTES_PER_CHAR);
        this.dev.read(offset, buffer);
        this.buffer.rewind();

        return getChar(buffer);
    }

    public void read(ByteBuffer dest, long offset) throws IOException {
        dev.read(offset, dest);
    }

}
