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
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.util.BigEndian;
import org.jnode.util.NumberUtils;

/**
 * HFS+ volume header definition.
 *
 * @author Fabien Lesire
 *
 */
public class SuperBlock extends HfsPlusObject {

    public static final int HFSPLUS_SUPER_MAGIC = 0x482b; // H+
    public static final int HFSX_SUPER_MAGIC = 0x4858; // HX

    public static final int HFSPLUS_MIN_VERSION = 0x0004; /* HFS+ */
    public static final int HFSPLUS_CURRENT_VERSION = 5; /* HFSX */

    /* HFS+ volume attributes */
    public static final int HFSPLUS_VOL_UNMNT_BIT = 8;
    public static final int HFSPLUS_VOL_SPARE_BLK_BIT = 9;
    public static final int HFSPLUS_VOL_NOCACHE_BIT = 10;
    public static final int HFSPLUS_VOL_INCNSTNT_BIT = 11;
    public static final int HFSPLUS_VOL_NODEID_REUSED_BIT = 12;
    public static final int HFSPLUS_VOL_JOURNALED_BIT = 13;
    public static final int HFSPLUS_VOL_SOFTLOCK_BIT = 15;

    private static final Logger log = Logger.getLogger(SuperBlock.class);

    /**
     * Volume header data length
     */
    public static final int SUPERBLOCK_LENGTH = 1024;

    /** Data bytes array that contains volume header information */
    private byte[] data;

    /**
     * Create the volume header and load information for the file system passed
     * as parameter.
     * 
     * @param fs The file system contains HFS+ partition.
     * 
     * @throws FileSystemException If magic number (0X482B) is incorrect or not
     *             available.
     */
    public SuperBlock(final HfsPlusFileSystem fs, boolean create) throws FileSystemException {
        super(fs);
        data = new byte[SUPERBLOCK_LENGTH];
        try {
            if (!create) {
                log.debug("load HFS+ volume header.");
                // skip the first 1024 bytes (boot sector) and read the volume
                // header.
                ByteBuffer b = ByteBuffer.allocate(SUPERBLOCK_LENGTH);
                fs.getApi().read(1024, b);
                data = new byte[SUPERBLOCK_LENGTH];
                System.arraycopy(b.array(), 0, data, 0, SUPERBLOCK_LENGTH);
                if (getMagic() != HFSPLUS_SUPER_MAGIC && getMagic() != HFSX_SUPER_MAGIC) {
                    throw new FileSystemException("Not hfs+ volume header (" + getMagic() +
                        ": bad magic)");
                }

            }
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Create a new volume header.
     * 
     * @param params File system format parameters.
     * @throws IOException
     */
    public void create(HFSPlusParams params) throws IOException {
        log.debug("Create new HFS+ volume header (" + params.getVolumeName() +
            ") with block size of " + params.getBlockSize() + " bytes.");
        int burnedBlocksBeforeVH = 0;
        int burnedBlocksAfterAltVH = 0;
        /*
         * Volume header is located at sector 2. Block before this position must
         * be invalidated.
         */
        int blockSize = params.getBlockSize();
        if (blockSize == 512) {
            burnedBlocksBeforeVH = 2;
            burnedBlocksAfterAltVH = 1;
        } else if (blockSize == 1024) {
            burnedBlocksBeforeVH = 1;
        }
        // Populate volume header.
        this.setMagic(HFSPLUS_SUPER_MAGIC);
        this.setVersion(HFSPLUS_MIN_VERSION);
        // Set attributes.
        this.setAttribute(HFSPLUS_VOL_UNMNT_BIT);
        this.setLastMountedVersion(0x446534a);
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        int macDate = HfsUtils.getNow();
        this.setCreateDate(macDate);
        this.setModifyDate(macDate);
        this.setCheckedDate(macDate);
        // ---
        this.setBlockSize(blockSize);
        this.setTotalBlocks((int) params.getBlockCount());
        this.setFreeBlocks((int) params.getBlockCount());
        this.setRsrcClumpSize(params.getResourceClumpSize());
        this.setDataClumpSize(params.getDataClumpSize());
        this.setNextCatalogId(CatalogNodeId.HFSPLUS_FIRSTUSER_CNID.getId());
        // Allocation file creation
        log.debug("Init allocation file.");
        long allocationClumpSize = getClumpSize(params.getBlockCount());
        long bitmapBlocks = allocationClumpSize / blockSize;
        long blockUsed = 2 + burnedBlocksBeforeVH + burnedBlocksAfterAltVH + bitmapBlocks;
        int startBlock = 1 + burnedBlocksBeforeVH;
        int blockCount = (int) bitmapBlocks;
        HfsPlusForkData forkdata =
            new HfsPlusForkData(CatalogNodeId.HFSPLUS_ALLOC_CNID, allocationClumpSize, (int) allocationClumpSize,
                (int) bitmapBlocks);
        ExtentDescriptor desc = new ExtentDescriptor(startBlock, blockCount);
        forkdata.addDescriptor(0, desc);
        forkdata.write(data, 112);
        // Journal creation
        long nextBlock = 0;
        if (params.isJournaled()) {
            this.setFileCount(2);
            this.setAttribute(HFSPLUS_VOL_JOURNALED_BIT);
            this.setNextCatalogId(this.getNextCatalogId() + 2);
            this.setJournalInfoBlock(desc.getNext());
            blockUsed = blockUsed + 1 + (params.getJournalSize() / blockSize);
        } else {
            this.setJournalInfoBlock(0);
            nextBlock = desc.getNext();
        }
        // Extent B-Tree initialization
        log.debug("Init extent file.");
        forkdata =
            new HfsPlusForkData(CatalogNodeId.HFSPLUS_EXT_CNID, params.getExtentClumpSize(),
                params.getExtentClumpSize(), (params.getExtentClumpSize() / blockSize));
        desc = new ExtentDescriptor(nextBlock, forkdata.getTotalBlocks());
        forkdata.addDescriptor(0, desc);
        forkdata.write(data, 192);
        blockUsed += forkdata.getTotalBlocks();
        nextBlock = desc.getNext();
        // Catalog B-Tree initialization
        log.debug("Init catalog file.");
        int totalBlocks = params.getCatalogClumpSize() / blockSize;
        forkdata =
            new HfsPlusForkData(CatalogNodeId.HFSPLUS_CAT_CNID, params.getCatalogClumpSize(),
                params.getCatalogClumpSize(), totalBlocks);
        desc = new ExtentDescriptor(nextBlock, totalBlocks);
        forkdata.addDescriptor(0, desc);
        forkdata.write(data, 272);
        blockUsed += totalBlocks;

        this.setFreeBlocks(this.getFreeBlocks() - (int) blockUsed);
        this.setNextAllocation((int) (blockUsed - 1 - burnedBlocksAfterAltVH + 10 *
            (this.getCatalogFile().getClumpSize() / this.getBlockSize())));
    }

    /**
     * Calculate the number of blocks needed for bitmap.
     * 
     * @param totalBlocks Total of blocks found in the device.
     * @return the number of blocks.
     * @throws IOException
     */
    private long getClumpSize(long totalBlocks) throws IOException {
        long clumpSize;
        long minClumpSize = totalBlocks >> 3;
        if ((totalBlocks & 7) == 0) {
            ++minClumpSize;
        }
        clumpSize = minClumpSize;
        return clumpSize;
    }

    // Getters/setters

    public final int getMagic() {
        return BigEndian.getUInt16(data, 0);
    }

    public final void setMagic(final int value) {
        BigEndian.setInt16(data, 0, value);
    }

    //
    public final int getVersion() {
        return BigEndian.getUInt16(data, 2);
    }

    public final void setVersion(final int value) {
        BigEndian.setInt16(data, 2, value);
    }

    //
    public final long getAttributes() {

        return BigEndian.getUInt32(data, 4);
    }

    public final void setAttribute(final int attributeMaskBit) {
        BigEndian.setInt32(data, 4, (int)(getAttributes() | (1 << attributeMaskBit)));
    }

    //
    public final long getLastMountedVersion() {
        return BigEndian.getUInt32(data, 8);
    }

    public final void setLastMountedVersion(final int value) {
        BigEndian.setInt32(data, 8, value);
    }

    //
    public final long getJournalInfoBlock() {
        return BigEndian.getUInt32(data, 12);
    }

    public final void setJournalInfoBlock(final long value) {
        BigEndian.setInt32(data, 12, (int) value);
    }

    //
    public final long getCreateDate() {
        return BigEndian.getUInt32(data, 16);
    }

    public final void setCreateDate(final int value) {
        BigEndian.setInt32(data, 16, value);
    }

    public final long getModifyDate() {
        return BigEndian.getUInt32(data, 20);
    }

    public final void setModifyDate(final int value) {
        BigEndian.setInt32(data, 20, value);
    }

    public final long getBackupDate() {
        return BigEndian.getUInt32(data, 24);
    }

    public final void setBackupDate(final int value) {
        BigEndian.setInt32(data, 24, value);
    }

    public final long getCheckedDate() {
        return BigEndian.getUInt32(data, 28);
    }

    public final void setCheckedDate(final int value) {
        BigEndian.setInt32(data, 28, value);
    }

    //
    public final long getFileCount() {
        return BigEndian.getUInt32(data, 32);
    }

    public final void setFileCount(final int value) {
        BigEndian.setInt32(data, 32, value);
    }

    //
    public final long getFolderCount() {
        return BigEndian.getUInt32(data, 36);
    }

    public final void setFolderCount(final long value) {
        BigEndian.setInt32(data, 36, (int) value);
    }

    //
    public final int getBlockSize() {
        return BigEndian.getInt32(data, 40);
    }

    public final void setBlockSize(final int value) {
        BigEndian.setInt32(data, 40, value);
    }

    //
    public final long getTotalBlocks() {
        return BigEndian.getUInt32(data, 44);
    }

    public final void setTotalBlocks(final int value) {
        BigEndian.setInt32(data, 44, value);
    }

    //
    public final long getFreeBlocks() {
        return BigEndian.getUInt32(data, 48);
    }

    public final void setFreeBlocks(final long value) {
        BigEndian.setInt32(data, 48, (int) value);
    }

    //
    public final long getNextAllocation() {
        return BigEndian.getUInt32(data, 52);
    }

    public final void setNextAllocation(final int value) {
        BigEndian.setInt32(data, 52, value);
    }

    public final long getRsrcClumpSize() {
        return BigEndian.getUInt32(data, 56);
    }

    public final void setRsrcClumpSize(final int value) {
        BigEndian.setInt32(data, 56, value);
    }

    public final long getDataClumpSize() {
        return BigEndian.getUInt32(data, 60);
    }

    public final void setDataClumpSize(final int value) {
        BigEndian.setInt32(data, 60, value);
    }

    public final long getNextCatalogId() {
        return BigEndian.getUInt32(data, 64);
    }

    public final void setNextCatalogId(final long value) {
        BigEndian.setInt32(data, 64, (int) value);
    }

    public final long getWriteCount() {
        return BigEndian.getUInt32(data, 68);
    }

    public final void setWriteCount(final int value) {
        BigEndian.setInt32(data, 68, value);
    }

    public final long getEncodingsBmp() {
        return BigEndian.getInt64(data, 72);
    }

    public final void setEncodingsBmp(final long value) {
        BigEndian.setInt64(data, 72, value);
    }

    public final byte[] getFinderInfo() {
        byte[] result = new byte[32];
        System.arraycopy(data, 80, result, 0, 32);
        return result;
    }

    public final HfsPlusForkData getAllocationFile() {
        return new HfsPlusForkData(CatalogNodeId.HFSPLUS_ALLOC_CNID, true, data, 112);
    }

    public final HfsPlusForkData getExtentsFile() {
        return new HfsPlusForkData(CatalogNodeId.HFSPLUS_EXT_CNID, true, data, 192);
    }

    public final HfsPlusForkData getCatalogFile() {
        return new HfsPlusForkData(CatalogNodeId.HFSPLUS_CAT_CNID, true, data, 272);
    }

    public final HfsPlusForkData getAttributesFile() {
        return new HfsPlusForkData(CatalogNodeId.HFSPLUS_ATTR_CNID, true, data, 352);
    }

    public final HfsPlusForkData getStartupFile() {
        return new HfsPlusForkData(CatalogNodeId.HFSPLUS_START_CNID, true, data, 432);
    }

    /**
     * Get string representation of attribute.
     * 
     * @return the string representation
     */
    public final String getAttributesAsString() {
        return ((isAttribute(HFSPLUS_VOL_UNMNT_BIT)) ? " kHFSVolumeUnmountedBit" : "") +
            ((isAttribute(HFSPLUS_VOL_INCNSTNT_BIT)) ? " kHFSBootVolumeInconsistentBit" : "") +
            ((isAttribute(HFSPLUS_VOL_JOURNALED_BIT)) ? " kHFSVolumeJournaledBit" : "");
    }

    /**
     * Check if the corresponding attribute corresponding is set.
     * 
     * @param maskBit Bit position of the attribute. See constants.
     * 
     * @return {@code true} if attribute is set.
     */
    public final boolean isAttribute(final int maskBit) {
        return (((getAttributes() >> maskBit) & 0x1) != 0);
    }

    public void incrementFolderCount() {
        this.setFolderCount(this.getFolderCount() + 1);
    }


    public byte[] getBytes() {
        return data;
    }

    public void update() throws IOException {
        fs.getApi().write(1024, ByteBuffer.wrap(data));
    }

    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Magic: 0x").append(NumberUtils.hex(getMagic(), 4)).append("\n");
        buffer.append("Version: ").append(getVersion()).append("\n").append("\n");
        buffer.append("Attributes: ").append(getAttributesAsString()).append(" (").append(
            getAttributes()).append(")").append("\n").append("\n");
        buffer.append("Create date: ").append(
            HfsUtils.printDate(getCreateDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Modify date: ").append(
            HfsUtils.printDate(getModifyDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Backup date: ").append(
            HfsUtils.printDate(getBackupDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Checked date: ").append(
            HfsUtils.printDate(getCheckedDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n")
            .append("\n");
        buffer.append("File count: ").append(getFileCount()).append("\n");
        buffer.append("Folder count: ").append(getFolderCount()).append("\n").append("\n");
        buffer.append("Block size: ").append(getBlockSize()).append("\n");
        buffer.append("Total blocks: ").append(getTotalBlocks()).append("\n");
        buffer.append("Free blocks: ").append(getFreeBlocks()).append("\n").append("\n");
        buffer.append("Next catalog ID: ").append(getNextCatalogId()).append("\n");
        buffer.append("Write count: ").append(getWriteCount()).append("\n");
        buffer.append("Encoding bmp: ").append(getEncodingsBmp()).append("\n");
        buffer.append("Finder Infos: ").append(getFinderInfo()).append("\n").append("\n");
        buffer.append("Journal block: ").append(getJournalInfoBlock()).append("\n").append("\n");
        buffer.append("Allocation file").append("\n");
        buffer.append(getAllocationFile().toString()).append("\n");
        buffer.append("Extents file").append("\n");
        buffer.append(getExtentsFile().toString()).append("\n");
        buffer.append("Catalog file").append("\n");
        buffer.append(getCatalogFile().toString()).append("\n");
        buffer.append("Attributes file").append("\n");
        buffer.append(getAttributesFile().toString()).append("\n");
        buffer.append("Startup file").append("\n");
        buffer.append(getStartupFile().toString()).append("\n");
        return buffer.toString();
    }
}
