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
 * An ext4 extent index.
 *
 * @author Luke Quinane
 */
public class ExtentIndex {
    /**
     * The length of an extent index.
     */
    public static final int EXTENT_INDEX_LENGTH = 12;

    /**
     * The data for the index.
     */
    private final byte[] data;

    /**
     * Create an extent index object.
     *
     * @param data the data for the index.
     */
    public ExtentIndex(byte[] data) {
        this.data = new byte[EXTENT_INDEX_LENGTH];
        System.arraycopy(data, 0, this.data, 0, EXTENT_INDEX_LENGTH);

        // Safety check
        if (getLeafHigh() != 0) {
            throw new UnsupportedOperationException("Extent indexes that use the high bits aren't supported yet");
        }
    }

    public long getBlockIndex() {
        return LittleEndian.getUInt32(data, 0);
    }

    public long getLeafLow() {
        return LittleEndian.getUInt32(data, 4);
    }

    public int getLeafHigh() {
        return LittleEndian.getUInt16(data, 8);
    }

    @Override
    public String toString() {
        return String.format("ExtentIndex: blockindex:%d leaf(low:%d high:%d)", getBlockIndex(), getLeafLow(),
                             getLeafHigh());
    }
}
