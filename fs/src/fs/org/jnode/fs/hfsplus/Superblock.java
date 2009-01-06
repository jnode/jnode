package org.jnode.fs.hfsplus;

import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_SUPER_MAGIC;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_INCNSTNT_BIT;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_JOURNALED_BIT;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_UNMNT_BIT;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.util.BigEndian;
import org.jnode.util.NumberUtils;

/**
 * HFS+ volume header definition.
 * 
 * @author Fabien L.
 * 
 */
public class Superblock extends HFSPlusObject {
    private final Logger log = Logger.getLogger(getClass());

    /** Volume header data length */
    public static final int SUPERBLOCK_LENGTH = 1024;

    /** Data bytes array that contains volume header information */
    private byte[] data;

    /**
     * Create the volume header and load information for the file system passed
     * as parameter.
     * 
     * @param fs
     *            The file system contains HFS+ partition.
     * 
     * @throws FileSystemException
     *             If magic number (0X482B) is incorrect or not available.
     */
    public Superblock(final HfsPlusFileSystem fs, boolean create) throws FileSystemException {
        super(fs);
        log.setLevel(Level.INFO);
        data = new byte[SUPERBLOCK_LENGTH];
        try {
            if (!create) {
                // skip the first 1024 bytes (boot sector) and read the volume
                // header.
                ByteBuffer b = ByteBuffer.allocate(SUPERBLOCK_LENGTH);
                fs.getApi().read(1024, b);
                data = new byte[SUPERBLOCK_LENGTH];
                System.arraycopy(b.array(), 0, data, 0, SUPERBLOCK_LENGTH);
                if (getMagic() != HFSPLUS_SUPER_MAGIC) {
                    throw new FileSystemException("Not hfs+ volume header (" + getMagic() + ": bad magic)");
                }
            }
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Create a new volume header.
     * 
     * @param params
     * 
     * @throws ApiNotFoundException
     */
    public void create(HFSPlusParams params) throws IOException,
            ApiNotFoundException, FileSystemException {
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
        long size = fs.getApi().getLength();
        long sectorCount = size / fs.getFSApi().getSectorSize();
        long blocks = size / blockSize;
        long allocationClumpSize = getClumpSize(blocks);
        long bitmapBlocks = allocationClumpSize / blockSize;
        long blockUsed = 2 + burnedBlocksBeforeVH + burnedBlocksAfterAltVH + bitmapBlocks;
        // Populate volume header.
        this.setMagic(HfsPlusConstants.HFSPLUS_SUPER_MAGIC);
        this.setVersion(HfsPlusConstants.HFSPLUS_MIN_VERSION);
        // Set attributes.
        this.setAttribute(HFSPLUS_VOL_UNMNT_BIT);
        this.setLastMountedVersion(0x446534a);
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        int macDate = (int) HFSUtils.getDate(now.getTimeInMillis() / 1000, true);
        this.setCreateDate(macDate);
        this.setModifyDate(macDate);
        this.setBackupDate(0);
        this.setCheckedDate(macDate);
        // ---
        this.setFileCount(0);
        this.setFolderCount(0);
        this.setBlockSize(blockSize);
        this.setTotalBlocks((int) blocks);
        this.setFreeBlocks((int) blocks);
        this.setRsrcClumpSize(HfsPlusConstants.RESOURCE_CLUMP_FACTOR * blockSize);
        this.setDataClumpSize(HfsPlusConstants.DATA_CLUMP_FACTOR * blockSize);
        this.setNextCatalogId(CatalogNodeId.HFSPLUS_FIRSTUSER_CNID.getId());
        // Allocation file creation
        initAllocationFile((int) allocationClumpSize, (int) bitmapBlocks, burnedBlocksBeforeVH);
        int nextBlock = 0;
        // Journal creation
        ExtentDescriptor desc = this.getAllocationFile().getExtents()[0];
        if (params.isJournaled()) {
            this.setFileCount(2);
            this.setAttribute(HFSPLUS_VOL_JOURNALED_BIT);
            this.setNextCatalogId(this.getNextCatalogId() + 2);
            this.setJournalInfoBlock(desc.getStartBlock() + desc.getBlockCount());
            blockUsed = blockUsed + 1 + (params.getJournalSize() / blockSize);
        } else {
            this.setJournalInfoBlock(0);
            nextBlock = desc.getStartBlock() + desc.getBlockCount();
        }
        blockUsed += initExtents(0, blockSize, nextBlock, (int) sectorCount, blockUsed);
        blockUsed += initCatalog(0, blockSize, nextBlock, (int) sectorCount, blockUsed);
        this.setFreeBlocks(this.getFreeBlocks() - (int) blockUsed);
        this.setNextAllocation((int) blockUsed - 1 - burnedBlocksAfterAltVH + 10
                * (this.getCatalogFile().getClumpSize() / this.getBlockSize()));
    }

    /**
     * 
     * @param clumpSize
     * @param bitmapBlocks
     * @param burnedBlocksBeforeVH
     * @return
     */
    private void initAllocationFile(int clumpSize, int bitmapBlocks, int burnedBlocksBeforeVH) {
        HFSPlusForkData forkdata = new HFSPlusForkData();
        forkdata.setTotalSize(clumpSize);
        forkdata.setClumpSize(clumpSize);
        forkdata.setTotalBlocks(bitmapBlocks);
        ExtentDescriptor desc = new ExtentDescriptor();
        desc.setStartBlock(1 + burnedBlocksBeforeVH);
        desc.setBlockCount(0);
        forkdata.setExtentDescriptor(0, desc);
        System.arraycopy(forkdata.getBytes(), 0, data, 112, forkdata.FORK_DATA_LENGTH);
    }

    /**
     * 
     * @param extentsClumpBlock
     * @param blockSize
     * @param nextBlock
     * @return
     */
    private long initExtents(int extentsClumpBlock, int blockSize, int nextBlock, int sectorCount, long blockUsed) {
        int extentNodeSize = 4096;
        long clumpSize = 0;
        if (extentsClumpBlock == 0) {
            clumpSize = getBTreeClumpSize(blockSize, extentNodeSize, sectorCount, false);
        } else {
            clumpSize = clumpSizeCalculation(extentsClumpBlock, blockSize);
        }
        HFSPlusForkData forkdata = new HFSPlusForkData();
        forkdata.setTotalSize(clumpSize);
        forkdata.setClumpSize((int) clumpSize);
        forkdata.setTotalBlocks((int) (clumpSize / blockSize));
        ExtentDescriptor desc = new ExtentDescriptor();
        desc.setStartBlock(nextBlock);
        desc.setBlockCount(forkdata.getTotalBlocks());
        forkdata.setExtentDescriptor(0, desc);
        System.arraycopy(forkdata.getBytes(), 0, data, 192, forkdata.FORK_DATA_LENGTH);
        return blockUsed + forkdata.getTotalBlocks();
    }

    /**
     * 
     * @param extentsClumpBlock
     * @param blockSize
     * @param nextBlock
     * @param sectorCount
     * @param blockUsed
     * @return
     * @throws IOException
     */
    private long initCatalog(int catalogClumpBlock, int blockSize, int nextBlock, int sectorCount, long blockUsed)
        throws FileSystemException {
        int catalogNodeSize = 8192;
        try {
            if (blockSize < HfsPlusConstants.OPTIMAL_BLOCK_SIZE || fs.getApi().getLength() < 0x40000000) {
                catalogNodeSize = 4096;
            }
            long clumpSize = 0;
            if (catalogClumpBlock == 0) {
                clumpSize = getBTreeClumpSize(blockSize, catalogNodeSize, sectorCount, true);
            } else {
                clumpSize = clumpSizeCalculation(catalogClumpBlock, blockSize);
                if (clumpSize % catalogNodeSize != 0) {
                    throw new FileSystemException("clump size is not a multiple of node size");
                }
            }

            HFSPlusForkData forkdata = new HFSPlusForkData();
            forkdata.setTotalSize(clumpSize);
            forkdata.setClumpSize((int) clumpSize);
            forkdata.setTotalBlocks((int) (clumpSize / blockSize));
            ExtentDescriptor desc = new ExtentDescriptor();
            desc.setStartBlock(this.getExtentsFile().getExtents()[0].getStartBlock()
                    + this.getExtentsFile().getExtents()[0].getBlockCount());
            desc.setBlockCount(forkdata.getTotalBlocks());
            forkdata.setExtentDescriptor(0, desc);
            System.arraycopy(forkdata.getBytes(), 0, data, 272, forkdata.FORK_DATA_LENGTH);
            return blockUsed + forkdata.getTotalBlocks();
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Calculate the number of blocks needed for bitmap.
     * 
     * @param totalBlocks
     *            Total of blocks found in the device.
     * 
     * @return long - Number of blocks.
     * 
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

    /**
     * 
     * @param blockSize
     * @param nodeSize
     * @param sectors
     * @param catalog
     * @return
     */

    private int[] extentClumpTable = new int[] {4, 4, 4, 5, 5, 6, 7, 8, 9, 11, 14, 16, 20, 25, 32 };
    private int[] catalogClumpTable = new int[] {4, 6, 8, 11, 14, 19, 25, 34, 45, 60, 80, 107, 144, 192, 256 };

    private long getBTreeClumpSize(int blockSize, int nodeSize, long sectors, boolean catalog) {
        long clumpSize = 0;
        if (sectors < 0x200000) {
            clumpSize = (sectors << 2);
            if (clumpSize < (8 * nodeSize)) {
                clumpSize = (8 * nodeSize);
            }
        } else {
            sectors = sectors >> 22;
            for (int i = 0; sectors != 0 && (i < 14); ++i) {
                if (catalog) {
                    clumpSize = catalogClumpTable[i] * 1024 * 1024;
                } else {
                    clumpSize = extentClumpTable[i] * 1024 * 1024;
                }
                sectors = sectors >> 1;
            }
        }

        return clumpSize;
    }

    private int clumpSizeCalculation(long clumpBlocks, int blockSize) {
        long clumpSize = clumpBlocks * blockSize;
        if ((clumpSize & 0XFFFFFFFF00000000L) == 0) {
            // ERROR
        }
        return (int) clumpSize;
    }

    // Getters/setters

    public final int getMagic() {
        return BigEndian.getInt16(data, 0);
    }

    public final void setMagic(final int value) {
        BigEndian.setInt16(data, 0, value);
    }

    //
    public final int getVersion() {
        return BigEndian.getInt16(data, 2);
    }

    public final void setVersion(final int value) {
        BigEndian.setInt16(data, 2, value);
    }

    //
    public final int getAttributes() {

        return BigEndian.getInt32(data, 4);
    }

    public final void setAttribute(final int attributeMaskBit) {
        BigEndian.setInt32(data, 4, (getAttributes() >> attributeMaskBit) | 0x1);
    }

    //
    public final int getLastMountedVersion() {
        return BigEndian.getInt32(data, 8);
    }

    public final void setLastMountedVersion(final int value) {
        BigEndian.setInt32(data, 8, value);
    }

    //
    public final int getJournalInfoBlock() {
        return BigEndian.getInt32(data, 12);
    }

    public final void setJournalInfoBlock(final int value) {
        BigEndian.setInt32(data, 12, value);
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
    public final int getFileCount() {
        return BigEndian.getInt32(data, 32);
    }

    public final void setFileCount(final int value) {
        BigEndian.setInt32(data, 32, value);
    }

    //
    public final int getFolderCount() {
        return BigEndian.getInt32(data, 36);
    }

    public final void setFolderCount(final int value) {
        BigEndian.setInt32(data, 36, value);
    }

    //
    public final int getBlockSize() {
        return BigEndian.getInt32(data, 40);
    }

    public final void setBlockSize(final int value) {
        BigEndian.setInt32(data, 40, value);
    }

    //
    public final int getTotalBlocks() {
        return BigEndian.getInt32(data, 44);
    }

    public final void setTotalBlocks(final int value) {
        BigEndian.setInt32(data, 44, value);
    }

    //
    public final int getFreeBlocks() {
        return BigEndian.getInt32(data, 48);
    }

    public final void setFreeBlocks(final int value) {
        BigEndian.setInt32(data, 48, value);
    }

    //
    public final int getNextAllocation() {
        return BigEndian.getInt32(data, 52);
    }

    public final void setNextAllocation(final int value) {
        BigEndian.setInt32(data, 52, value);
    }

    public final long getRsrcClumpSize() {
        return BigEndian.getInt32(data, 56);
    }

    public final void setRsrcClumpSize(final int value) {
        BigEndian.setInt32(data, 56, value);
    }

    public final int getDataClumpSize() {
        return BigEndian.getInt32(data, 60);
    }

    public final void setDataClumpSize(final int value) {
        BigEndian.setInt32(data, 60, value);
    }

    public final int getNextCatalogId() {
        return BigEndian.getInt32(data, 64);
    }

    public final void setNextCatalogId(final int value) {
        BigEndian.setInt32(data, 64, value);
    }

    public final int getWriteCount() {
        return BigEndian.getInt32(data, 68);
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

    public final HFSPlusForkData getAllocationFile() {
        return new HFSPlusForkData(data, 112);
    }

    public final HFSPlusForkData getExtentsFile() {
        return new HFSPlusForkData(data, 192);
    }

    public final HFSPlusForkData getCatalogFile() {
        return new HFSPlusForkData(data, 272);
    }

    public final HFSPlusForkData getAttributesFile() {
        return new HFSPlusForkData(data, 352);
    }

    public final HFSPlusForkData getStartupFile() {
        return new HFSPlusForkData(data, 432);
    }

    /**
     * Get string representation of attribute.
     * 
     * @return
     */
    public final String getAttributesAsString() {
        return ((isAttribute(HFSPLUS_VOL_UNMNT_BIT)) ? " kHFSVolumeUnmountedBit" : "")
                + ((isAttribute(HFSPLUS_VOL_INCNSTNT_BIT)) ? " kHFSBootVolumeInconsistentBit" : "")
                + ((isAttribute(HFSPLUS_VOL_JOURNALED_BIT)) ? " kHFSVolumeJournaledBit" : "");
    }

    /**
     * Check if the corresponding attribute corresponding is set.
     * 
     * @param maskBit
     *            Bit position of the attribute. See constants.
     * 
     * @return true if attribute is set.
     */
    public final boolean isAttribute(final int maskBit) {
        return (((getAttributes() >> maskBit) & 0x1) != 0);
    }

    public byte[] getBytes() {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Magic: 0x").append(NumberUtils.hex(getMagic(), 4)).append("\n");
        buffer.append("Version: ").append(getVersion()).append("\n").append("\n");
        buffer.append("Attributes: ").append(getAttributesAsString()).append(" (").append(getAttributes()).append(")")
                .append("\n").append("\n");
        buffer.append("Create date: ").append(HFSUtils.printDate(getCreateDate(), "EEE MMM d HH:mm:ss yyyy")).append(
                "\n");
        buffer.append("Modify date: ").append(HFSUtils.printDate(getModifyDate(), "EEE MMM d HH:mm:ss yyyy")).append(
                "\n");
        buffer.append("Backup date: ").append(HFSUtils.printDate(getBackupDate(), "EEE MMM d HH:mm:ss yyyy")).append(
                "\n");
        buffer.append("Checked date: ").append(HFSUtils.printDate(getCheckedDate(), "EEE MMM d HH:mm:ss yyyy")).append(
                "\n").append("\n");
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
