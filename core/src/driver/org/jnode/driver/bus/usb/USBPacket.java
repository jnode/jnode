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

package org.jnode.driver.bus.usb;

import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBPacket implements USBConstants {

    private final byte[] data;
    private final int size;
    private final int offset;

    /**
     * Create a new instance
     *
     * @param size
     */
    public USBPacket(int size) {
        this.data = new byte[size];
        this.size = size;
        this.offset = 0;
    }

    /**
     * Create a new instance
     *
     * @param data
     */
    public USBPacket(byte[] data) {
        this(data, 0, data.length);
    }

    /**
     * Create a new instance
     *
     * @param data
     * @param ofs
     * @param len
     */
    public USBPacket(byte[] data, int ofs, int len) {
        this.data = data;
        this.size = len;
        this.offset = ofs;
    }

    /**
     * Set a 8-bit byte at the given offset
     *
     * @param ofs
     * @param value
     */
    protected final void setByte(int ofs, int value) {
        data[this.offset + ofs] = (byte) value;
    }

    /**
     * Set a 16-bit short at the given offset
     *
     * @param ofs
     * @param value
     */
    protected final void setShort(int ofs, int value) {
        data[this.offset + ofs + 0] = (byte) (value & 0xFF);
        data[this.offset + ofs + 1] = (byte) ((value >> 8) & 0xFF);
    }

    /**
     * Set a 32-bit int at the given offset
     *
     * @param ofs
     * @param value
     */
    protected final void setInt(int ofs, int value) {
        data[this.offset + ofs + 0] = (byte) (value & 0xFF);
        data[this.offset + ofs + 1] = (byte) ((value >> 8) & 0xFF);
        data[this.offset + ofs + 2] = (byte) ((value >> 16) & 0xFF);
        data[this.offset + ofs + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    /**
     * Get a 8-bit byte at the given offset
     *
     * @param ofs
     */
    protected final int getByte(int ofs) {
        return data[this.offset + ofs] & 0xFF;
    }

    /**
     * Get a 16-bit short at the given offset
     *
     * @param ofs
     */
    protected final int getShort(int ofs) {
        final int v0 = data[this.offset + ofs + 0] & 0xFF;
        final int v1 = data[this.offset + ofs + 1] & 0xFF;
        return (short) (v0 | (v1 << 8));
    }

    /**
     * Get a 16-bit char at the given offset
     *
     * @param ofs
     */
    protected final char getChar(int ofs) {
        final int v0 = data[this.offset + ofs + 0] & 0xFF;
        final int v1 = data[this.offset + ofs + 1] & 0xFF;
        return (char) (v0 | (v1 << 8));
    }

    /**
     * Get a 32-bit int at the given offset
     *
     * @param ofs
     */
    protected final int getInt(int ofs) {
        final int v0 = data[this.offset + ofs + 0] & 0xFF;
        final int v1 = data[this.offset + ofs + 1] & 0xFF;
        final int v2 = data[this.offset + ofs + 2] & 0xFF;
        final int v3 = data[this.offset + ofs + 3] & 0xFF;
        return (short) (v0 | (v1 << 8) | (v2 << 16) | (v3 << 24));
    }

    /**
     * Gets the data itself
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Gets the offset within the data
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Gets the length of the data
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Convert to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return NumberUtils.hex(data, offset, size);
    }
}
