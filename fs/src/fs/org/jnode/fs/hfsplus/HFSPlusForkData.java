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

package org.jnode.fs.hfsplus;

import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.util.BigEndian;

public class HFSPlusForkData {
    public static final int FORK_DATA_LENGTH = 80;
    private static final int EXTENT_OFFSET = 16;
    /** The size in bytes of the valid data in the fork. */
    private long totalSize;
    /** */
    private int clumpSize;
    /** The total of allocation blocks use by the extents in the fork. */
    private int totalBlock;
    /** The first eight extent descriptors for the fork. */
    private ExtentDescriptor[] extents;

    /**
     * Create fork data from existing informations.
     * 
     * @param src
     * @param offset
     */
    public HFSPlusForkData(final byte[] src, final int offset) {
        byte[] data = new byte[FORK_DATA_LENGTH];
        System.arraycopy(src, offset, data, 0, FORK_DATA_LENGTH);
        totalSize = BigEndian.getInt64(data, 0);
        clumpSize = BigEndian.getInt32(data, 8);
        totalBlock = BigEndian.getInt32(data, 12);
        extents = new ExtentDescriptor[8];
        for (int i = 0; i < 8; i++) {
            extents[i] =
                    new ExtentDescriptor(data, EXTENT_OFFSET +
                            (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
    }

    /**
     * 
     * Create a new empty fork data object.
     * 
     * @param totalSize
     * @param clumpSize
     * @param totalBock
     */
    public HFSPlusForkData(long totalSize, int clumpSize, int totalBlock) {
        this.totalSize = totalSize;
        this.clumpSize = clumpSize;
        this.totalBlock = totalBlock;
        this.extents = new ExtentDescriptor[8];
        for (int i = 0; i < extents.length; i++) {
            extents[i] = new ExtentDescriptor();
        }
    }

    public byte[] write(byte[] dest, int destOffSet) {
        byte[] data = new byte[FORK_DATA_LENGTH];
        BigEndian.setInt64(data, 0, totalSize);
        BigEndian.setInt32(data, 8, clumpSize);
        BigEndian.setInt32(data, 12, totalBlock);
        for (int i = 0; i < extents.length; i++) {
            extents[i].write(data, EXTENT_OFFSET + (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
        System.arraycopy(data, 0, dest, destOffSet, FORK_DATA_LENGTH);
        return dest;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Total size : ").append(totalSize).append("\n");
        s.append("Clump size : ").append(clumpSize).append("\n");
        s.append("Total Blocks : ").append(totalBlock).append("\n");
        for (int i = 0; i < extents.length; i++) {
            s.append("Extent[" + i + "]: " + extents[i].toString());
        }
        return s.toString();
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getClumpSize() {
        return clumpSize;
    }

    public int getTotalBlocks() {
        return totalBlock;
    }

    public ExtentDescriptor getExtent(int index) {
        return extents[index];
    }

    /**
     * 
     * @param index
     * @param desc
     */
    public final void addDescriptor(int index, ExtentDescriptor desc) {
        extents[index] = desc;
    }

    public ExtentDescriptor[] getExtents() {
        return extents;
    }

}
