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
 
package org.jnode.fs.hfsplus.extent;

import org.jnode.util.BigEndian;

public class ExtentDescriptor {
    /** The size of the extent descriptor. */
    public static final int EXTENT_DESCRIPTOR_LENGTH = 8;
    
    /** The first allocation block. */
    private long startBlock;
    
    /** The length in allocation blocks of the extent. */
    private long blockCount;

    public ExtentDescriptor() {
        this.startBlock = 0;
        this.blockCount = 0;
    }

    /**
     * Create a new extent descriptor.
     * 
     * @param startBlock first allocation block.
     * @param blockCount number of blocks in the extent.
     */
    public ExtentDescriptor(long startBlock, long blockCount) {
        this.startBlock = startBlock;
        this.blockCount = blockCount;
    }

    /**
     * Create extent descriptor from existing data.
     * 
     * @param src byte array contains existing extent descriptor informations.
     * @param offset position where data for extent descriptor begin.
     */
    public ExtentDescriptor(final byte[] src, final int offset) {
        byte[] data = new byte[EXTENT_DESCRIPTOR_LENGTH];
        System.arraycopy(src, offset, data, 0, EXTENT_DESCRIPTOR_LENGTH);
        startBlock = BigEndian.getUInt32(data, 0);
        blockCount = BigEndian.getUInt32(data, 4);
    }

    /**
     * @return the extent encoded as data
     */
    public final byte[] getBytes() {
        byte[] data = new byte[EXTENT_DESCRIPTOR_LENGTH];
        BigEndian.setInt32(data, 0, (int) startBlock);
        BigEndian.setInt32(data, 4, (int) blockCount);
        return data;
    }

    public byte[] write(byte[] dest, int destOffSet) {
        byte[] data = new byte[EXTENT_DESCRIPTOR_LENGTH];
        BigEndian.setInt32(data, 0, (int) startBlock);
        BigEndian.setInt32(data, 4, (int) blockCount);
        System.arraycopy(data, 0, dest, destOffSet, EXTENT_DESCRIPTOR_LENGTH);
        return dest;
    }

    @Override
    public final String toString() {
        return String.format("extent-desc:[start-block:%d count:%d]", startBlock, blockCount);
    }

    /**
     * Returns start block of the extent.
     *
     * @return the start block.
     */
    public long getStartBlock() {
        return startBlock;
    }

    /**
     * Returns start position in bytes of the extent.
     *
     * @param nodeSize the size of a node.
     * @return offset of the extent.
     */
    public long getStartOffset(int nodeSize) {
        return startBlock * nodeSize;
    }

    /**
     * Returns block number of the next extent.
     * 
     * @return block number of the next extent.
     */
    public long getNext() {
        return startBlock + blockCount;
    }

    /**
     * Returns size in byte of the extent.
     * 
     * @param nodeSize the size of a node.
     * @return size of the extent.
     */
    public long getSize(int nodeSize) {
        return blockCount * nodeSize;
    }

    /**
     * Returns the block count.
     *
     * @return the block count.
     */
    public long getBlockCount() {
        return blockCount;
    }

    /**
     * Returns <tt>true</tt> if the extent is empty.
     * 
     * @return <tt>true</tt> if the extent is empty.
     */
    public boolean isEmpty() {
        return (startBlock == 0 || blockCount == 0);
    }

}
