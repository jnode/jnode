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
 
package org.jnode.fs.ext4;

import org.jnode.util.LittleEndian;

/**
 * An ext4 extent object.
 *
 * @author Luke Quinane
 */
public class Extent {
    /**
     * The length of an extent.
     */
    public static final int EXTENT_LENGTH = 12;

    /**
     * The data for the extent.
     */
    private byte[] data;

    /**
     * Create an extent object.
     *
     * @param data the data for the extent.
     */
    public Extent(byte[] data) {
        this.data = new byte[EXTENT_LENGTH];
        System.arraycopy(data, 0, this.data, 0, EXTENT_LENGTH);

        // Safety check
        if (getStartHigh() != 0) {
            throw new UnsupportedOperationException("Extents that use the high bits aren't supported yet");
        }
    }

    public long getBlockIndex() {
        return LittleEndian.getUInt32(data, 0);
    }

    public int getBlockCount() {
        return LittleEndian.getUInt16(data, 4);
    }

    public long getStartLow() {
        return LittleEndian.getUInt32(data, 8);
    }

    public int getStartHigh() {
        return LittleEndian.getUInt16(data, 6);
    }

    @Override
    public String toString() {
        return String.format("Extent: blockindex:%d count:%d start(low:%d high:%d)", getBlockIndex(), getBlockCount(),
                             getStartLow(), getStartHigh());
    }
}
