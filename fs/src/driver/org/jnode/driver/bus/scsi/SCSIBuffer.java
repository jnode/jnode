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

package org.jnode.driver.bus.scsi;

import java.io.UnsupportedEncodingException;
import org.jnode.util.BigEndian;
import org.jnode.util.NumberUtils;

/**
 * Generic byte buffer for use in SCSI buffer.
 * All multi-byte values are treated as MSB first (big-endian).
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SCSIBuffer extends BigEndian {

    private final byte[] buffer;

    /**
     * Initialize this instance.
     *
     * @param bufLength
     */
    public SCSIBuffer(int bufLength) {
        this.buffer = new byte[bufLength];
    }

    /**
     * Initialize this instance.
     *
     * @param buffer
     */
    public SCSIBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    /**
     * Set a 8-bit integer at a given offset.
     *
     * @param offset
     * @param v
     */
    public final void setInt8(int offset, int v) {
        setInt8(buffer, offset, v);
    }

    /**
     * Gets a 8-bit integer from a given offset.
     * The byte is returned as 8-bit unsigned integer (0..0xFF).
     *
     * @param offset
     * @return
     */
    public final int getUInt8(int offset) {
        return getUInt8(buffer, offset);
    }

    /**
     * Set a 16-bit integer at a given offset.
     *
     * @param offset
     * @param v
     */
    public final void setInt16(int offset, int v) {
        setInt16(buffer, offset, v);
    }

    /**
     * Gets a 16-bit integer from a given offset.
     * The byte is returned as 16-bit unsigned integer (0..0xFFFF).
     *
     * @param offset
     * @return
     */
    public final int getUInt16(int offset) {
        return getUInt16(buffer, offset);
    }

    /**
     * Set an 32-bit integer at a given offset.
     *
     * @param offset
     * @param v
     */
    public final void setInt32(int offset, int v) {
        setInt32(buffer, offset, v);
    }

    /**
     * Gets a 32-bit integer from a given offset.
     * The byte is returned as 32-bit signed integer (Integer.MIN_VALUE..Integer.MAX_VALUE).
     *
     * @param offset
     * @return
     */
    public final int getInt32(int offset) {
        return getInt32(buffer, offset);
    }

    /**
     * Gets an ASCII string from the given offset with a given length.
     * The padded 0x20 (space) characters are removed.
     *
     * @param offset
     * @param length
     * @return
     */
    public final String getASCII(int offset, int length) {
        try {
            return new String(buffer, offset, length, "US-ASCII").trim();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the bytearray itself.
     *
     * @return
     */
    public final byte[] toByteArray() {
        return buffer;
    }

    /**
     * Convert an integer to a 2-character long hex string.
     *
     * @param v
     * @return
     */
    protected static final String hex2(int v) {
        return NumberUtils.hex(v, 2);
    }

    /**
     * Convert an integer to a 4-character long hex string.
     *
     * @param v
     * @return
     */
    protected static final String hex4(int v) {
        return NumberUtils.hex(v, 4);
    }

    /**
     * Convert an integer to a 8-character long hex string.
     *
     * @param v
     * @return
     */
    protected static final String hex8(int v) {
        return NumberUtils.hex(v, 8);
    }
}
