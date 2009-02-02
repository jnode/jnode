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
 
package org.jnode.fs.ext2;

import org.jnode.fs.FileSystemException;

/**
 * This class provides static methods that operate on the data as a bitmap
 * 
 * @author Andras Nagy
 */
public class FSBitmap {
    
    private static final boolean DEBUG = true;

    /**
     * Check if the block/inode is free according to the bitmap.
     * 
     * @param index the index of the block/inode relative to the block group
     *            (not relative to the whole fs)
     * @return true if the block/inode is free, false otherwise
     */
    protected static boolean isFree(byte[] data, int index) {
        int byteIndex = index / 8;
        byte bitIndex = (byte) (index % 8);
        byte mask = (byte) (1 << bitIndex);

        return ((data[byteIndex] & mask) == 0) ? true : false;
    }

    protected static boolean isFree(byte data, int index) {
        // byte bitIndex = (byte) (index % 8);

        byte mask = (byte) (1 << index);
        return ((data & mask) == 0) ? true : false;
    }

    protected static void setBit(byte[] data, int index) {
        int byteIndex = index / 8;
        byte bitIndex = (byte) (index % 8);
        byte mask = (byte) (1 << bitIndex);

        data[byteIndex] = (byte) (data[byteIndex] | mask);
    }

    protected static void setBit(byte[] data, int byteIndex, int bitIndex) {
        byte mask = (byte) (1 << bitIndex);

        data[byteIndex] = (byte) (data[byteIndex] | mask);
    }

    protected static void freeBit(byte[] data, int index) throws FileSystemException {
        int byteIndex = index / 8;
        byte bitIndex = (byte) (index % 8);
        byte mask = (byte) ~(1 << bitIndex);

        // filesystem consistency check
        if (DEBUG) {
            if (isFree(data[byteIndex], bitIndex))
                throw new FileSystemException("FS consistency error: you are trying "
                        + "to free an unallocated block/inode");
        }

        data[byteIndex] = (byte) (data[byteIndex] & mask);
    }

}
