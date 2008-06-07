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
 
package org.jnode.fs.ntfs;

import org.apache.log4j.Logger;
import org.jnode.util.LittleEndian;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class NTFSStructure {

    /** My logger */
    protected static final Logger log = Logger.getLogger(NTFSStructure.class);

    private byte[] buffer;
    private int offset;

    /**
     * Initialize this instance.
     * @param buffer
     * @param offset
     */
    NTFSStructure(byte[] buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    /**
     * Initialize this instance.
     * @param parent
     * @param offset
     */
    NTFSStructure(NTFSStructure parent, int offset) {
        this.buffer = parent.buffer;
        this.offset = parent.offset + offset;
    }

    /**
     * Re-initialize this instance.
     * @param buffer
     * @param offset
     */
    final void reset(byte[] buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    /**
     * Re-initialize this instance.
     * @param parent
     * @param offset
     */
    final void reset(NTFSStructure parent, int offset) {
        this.buffer = parent.buffer;
        this.offset = parent.offset + offset;
    }

    /**
     * Read an unsigned 8-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getUInt8(int offset) {
        return LittleEndian.getUInt8(buffer, this.offset + offset);
    }

    /**
     * Read an unsigned 16-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getUInt16(int offset) {
        return LittleEndian.getUInt16(buffer, this.offset + offset);
    }

    /**
     * Read an unsigned 24-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getUInt24(int offset) {
        return LittleEndian.getUInt24(buffer, this.offset + offset);
    }

    /**
     * Read an unsigned 32-bit integer from a given offset.
     * @param offset
     * @return
     */
    final long getUInt32(int offset) {
        return LittleEndian.getUInt32(buffer, this.offset + offset);
    }

    /**
     * Read an unsigned 32-bit integer from a given offset as a java int.
     * @param offset
     * @return
     */
    final int getUInt32AsInt(int offset) {
        return (int) LittleEndian.getUInt32(buffer, this.offset + offset);
    }

    /**
     * Read an unsigned 48-bit integer from a given offset.
     * @param offset
     * @return
     */
    final long getUInt48(int offset) {
        return LittleEndian.getUInt48(buffer, this.offset + offset);
    }

    /**
     * Read a signed 8-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getInt8(int offset) {
        return LittleEndian.getInt8(buffer, this.offset + offset);
    }

    /**
     * Read a signed 16-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getInt16(int offset) {
        return LittleEndian.getInt16(buffer, this.offset + offset);
    }

    /**
     * Read a signed 24-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getInt24(int offset) {
        return LittleEndian.getInt24(buffer, this.offset + offset);
    }

    /**
     * Read n signed 32-bit integer from a given offset.
     * @param offset
     * @return
     */
    final int getInt32(int offset) {
        return LittleEndian.getInt32(buffer, this.offset + offset);
    }

    /**
     * Read n signed 64-bit integer from a given offset.
     * @param offset
     * @return
     */
    final long getInt64(int offset) {
        return LittleEndian.getInt64(buffer, this.offset + offset);
    }

    /**
     * Copy (byte-array) data from a given offset.
     * @param offset
     * @param dst
     * @param dstOffset
     * @param length
     */
    final void getData(int offset, byte[] dst, int dstOffset, int length) {
        System.arraycopy(buffer, this.offset + offset, dst, dstOffset, length);
    }

    /**
     * Read an unsigned 16-bit unicode character from a given offset.
     * @param offset
     * @return
     */
    final char getChar16(int offset) {
        final int v0 = buffer[this.offset + offset] & 0xFF;
        final int v1 = buffer[this.offset + offset + 1] & 0xFF;
        return (char) ((v1 << 8) | v0);
    }

    /**
     * Write an unsigned 16-bit integer to a given offset.
     * @param offset
     */
    final void setUInt16(int offset, int value) {
        LittleEndian.setInt16(buffer, this.offset + offset, value);
    }

}
