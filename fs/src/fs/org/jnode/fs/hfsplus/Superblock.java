package org.jnode.fs.hfsplus;

import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_SUPER_MAGIC;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_INCNSTNT_BIT;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_JOURNALED_BIT;
import static org.jnode.fs.hfsplus.HfsPlusConstants.HFSPLUS_VOL_UNMNT_BIT;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;
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
    
    public static final int SUPERBLOCK_LENGTH = 1024;
    
    /** Data bytes array that contains superblock information */
    private byte[] data;

    public Superblock() {
        super(null);
        data = new byte[SUPERBLOCK_LENGTH];
        log.setLevel(Level.INFO);
    }

    public Superblock(final HfsPlusFileSystem fs) throws FileSystemException {
        super(fs);
        log.setLevel(Level.INFO);
        try {
            ByteBuffer b = ByteBuffer.allocate(SUPERBLOCK_LENGTH);
            // skip the first 1024 bytes (bootsector) and read the superblock
            fs.getApi().read(1024, b);
            data = new byte[SUPERBLOCK_LENGTH];
            System.arraycopy(b.array(), 0, data, 0, SUPERBLOCK_LENGTH);
            if (getMagic() != HFSPLUS_SUPER_MAGIC) {
                throw new FileSystemException("Not hfs+ superblock (" + getMagic() + ": bad magic)");
            }
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

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

    /**
     * Get string representation of attribute.
     * 
     * @return
     */
    public final String getAttributesAsString() {
        return ((isAttribute(HFSPLUS_VOL_UNMNT_BIT)) ? " kHFSVolumeUnmountedBit" : "") +
                ((isAttribute(HFSPLUS_VOL_INCNSTNT_BIT)) ? " kHFSBootVolumeInconsistentBit" : "") +
                ((isAttribute(HFSPLUS_VOL_JOURNALED_BIT)) ? " kHFSVolumeJournaledBit" : "");
    }

    /**
     * Check if a specific attribute is set.
     * 
     * @param maskBit See constants.
     * 
     * @return true if attribute is set.
     */
    public final boolean isAttribute(final int maskBit) {
        return (((getAttributes() >> maskBit) & 0x1) != 0);
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
    public final int getCreateDate() {
        return BigEndian.getInt32(data, 16);
    }

    public final void setCreateDate(final int value) {
        BigEndian.setInt32(data, 16, value);
    }

    public final int getModifyDate() {
        return BigEndian.getInt32(data, 20);
    }

    public final void setModifyDate(final int value) {
        BigEndian.setInt32(data, 20, value);
    }

    public final int getBackupDate() {
        return BigEndian.getInt32(data, 24);
    }

    public final void setBackupDate(final int value) {
        BigEndian.setInt32(data, 24, value);
    }

    public final int getCheckedDate() {
        return BigEndian.getInt32(data, 28);
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

    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Magic: 0x").append(NumberUtils.hex(getMagic(), 4)).append("\n");
        buffer.append("Version: ").append(getVersion()).append("\n").append("\n");
        buffer.append("Attributes: ").append(getAttributesAsString()).append("\n").append("\n");
        buffer.append("Create date: ").append(
                HFSUtils.printDate(getCreateDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Modify date: ").append(
                HFSUtils.printDate(getModifyDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Backup date: ").append(
                HFSUtils.printDate(getBackupDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        buffer.append("Checked date: ").append(
                HFSUtils.printDate(getCheckedDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n")
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
        buffer.append("Finder Infos: ").append(getJournalInfoBlock()).append("\n").append("\n");
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
