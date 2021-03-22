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
 
package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.extent.ExtentKey;
import org.jnode.fs.util.FSUtils;
import org.jnode.util.BigEndian;

public class HfsPlusForkData {
    public static final int FORK_DATA_LENGTH = 80;
    private static final int EXTENT_OFFSET = 16;

    /**
     * The logger.
     */
    protected static final Logger log = Logger.getLogger(HfsPlusForkData.class);

    /**
     * The size in bytes of the valid data in the fork.
     */
    private long totalSize;
    /** */
    private long clumpSize;
    /**
     * The total of allocation blocks use by the extents in the fork.
     */
    private long totalBlock;
    /**
     * The first eight extent descriptors for the fork.
     */
    private ExtentDescriptor[] extents;
    /**
     * Overflow extents.
     */
    private List<ExtentDescriptor> overflowExtents;

    /**
     * The catalog node ID that owns this fork.
     */
    private final CatalogNodeId cnid;

    /**
     * Indicates whether this is a data fork, or a resource fork.
     */
    private final boolean dataFork;

    /**
     * Create fork data from existing information.
     *
     * @param cnid the catalog node ID that owns this fork.
     * @param dataFork indicates whether this is a data fork, or a resource fork.
     * @param src
     * @param offset
     */
    public HfsPlusForkData(CatalogNodeId cnid, boolean dataFork, final byte[] src, final int offset) {
        this.cnid = cnid;
        this.dataFork = dataFork;
        byte[] data = new byte[FORK_DATA_LENGTH];
        System.arraycopy(src, offset, data, 0, FORK_DATA_LENGTH);
        totalSize = BigEndian.getInt64(data, 0);
        clumpSize = BigEndian.getUInt32(data, 8);
        totalBlock = BigEndian.getUInt32(data, 12);
        extents = new ExtentDescriptor[8];
        for (int i = 0; i < 8; i++) {
            extents[i] =
                new ExtentDescriptor(data, EXTENT_OFFSET +
                    (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
    }

    /**
     * Create a new empty data-fork data.
     *
     * @param totalSize
     * @param clumpSize
     * @param totalBlock
     */
    public HfsPlusForkData(CatalogNodeId cnid, long totalSize, int clumpSize, int totalBlock) {
        this.cnid = cnid;
        this.dataFork = true;
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
        BigEndian.setInt32(data, 8, (int) clumpSize);
        BigEndian.setInt32(data, 12, (int) totalBlock);
        for (int i = 0; i < extents.length; i++) {
            extents[i].write(data, EXTENT_OFFSET + (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
        System.arraycopy(data, 0, dest, destOffSet, FORK_DATA_LENGTH);
        return dest;
    }

    @Override
    public final String toString() {
        return String.format("HFS+ fork-data:[total-size:%d clump-size:%d total-blocks: %d extents:%d]",
            totalSize, clumpSize, totalBlock, extents.length);
    }

    public final String toDebugString() {
        StringBuffer s = new StringBuffer();
        s.append("Total size : ").append(totalSize).append("\n");
        s.append("Clump size : ").append(clumpSize).append("\n");
        s.append("Total Blocks : ").append(totalBlock).append("\n");
        for (int i = 0; i < extents.length; i++) {
            s.append("Extent[" + i + "]: " + extents[i].toString() + "\n");
        }
        return s.toString();
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getClumpSize() {
        return clumpSize;
    }

    public long getTotalBlocks() {
        return totalBlock;
    }

    public ExtentDescriptor getExtent(int index) {
        return extents[index];
    }

    /**
     * Gets all extents to read data from.
     *
     * @param fileSystem the current file system.
     * @return the collection of extents.
     * @throws IOException if an error occurs.
     */
    public Collection<ExtentDescriptor> getAllExtents(HfsPlusFileSystem fileSystem) throws IOException {
        List<ExtentDescriptor> allExtents = new ArrayList<ExtentDescriptor>();
        Collections.addAll(allExtents, extents);

        // Only check for overflow extents if the last non-overflow extent is in use
        if (!extents[7].isEmpty() && overflowExtents == null) {
            int forkType = dataFork ? ExtentKey.DATA_FORK : ExtentKey.RESOURCE_FORK;
            overflowExtents = fileSystem.getExtentOverflow().getOverflowExtents(new ExtentKey(forkType, 0, cnid, 0));
        }

        // Add the overflow extents if the exist
        if (overflowExtents != null) {
            allExtents.addAll(overflowExtents);
        }

        return allExtents;
    }

    /**
     * Read a block of data
     *
     * @param fileSystem the associated file system.
     * @param offset the offset to read from.
     * @param buffer the buffer to read into.
     * @throws java.io.IOException if an error occurs.
     */
    public void read(HfsPlusFileSystem fileSystem, long offset, ByteBuffer buffer) throws IOException {
        int blockSize = fileSystem.getVolumeHeader().getBlockSize();
        int limit = buffer.limit();
        int remaining = buffer.remaining();

        if (log.isDebugEnabled()) {
            log.debug("read: offset " + offset + " length " + buffer.remaining() + ": " + this);
        }

        Collection<ExtentDescriptor> allExtents = getAllExtents(fileSystem);

        for (ExtentDescriptor extentDescriptor : allExtents) {
            if (remaining > 0 && !extentDescriptor.isEmpty()) {
                long length = extentDescriptor.getSize(blockSize);

                if (offset != 0 && length < offset) {
                    offset -= length;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("reading: offset " + offset + " extent: " + extentDescriptor);
                    }

                    long firstOffset = extentDescriptor.getStartOffset(blockSize);

                    while (remaining > 0 && offset < length) {
                        int byteCount = Math.min(remaining, blockSize);
                        long extentRemaining = length - offset;
                        byteCount = FSUtils.checkedCast(Math.min(byteCount, extentRemaining));

                        // Sanity check
                        if (byteCount < 0) {
                            throw new IllegalStateException(
                                String.format("byteCount is -ve, offset:%d, length:%d, remaining:%d",
                                    offset, length, remaining));
                        }

                        buffer.limit(buffer.position() + byteCount);
                        fileSystem.getApi().read(firstOffset + offset, buffer);

                        offset += byteCount;
                        remaining -= byteCount;
                    }

                    offset = 0;
                }
            }
        }

        if (remaining > 0) {
            throw new IOException(String.format("Failed to read in all the data. cnid: %s offset: %d extents: %s",
                cnid, offset, allExtents));
        }

        // Reset the limit
        buffer.limit(limit);
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
