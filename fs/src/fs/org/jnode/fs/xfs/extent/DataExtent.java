package org.jnode.fs.xfs.extent;

import org.jnode.fs.xfs.XfsObject;

/**
 * A data extent ('xfs_bmbt_rec_t' packed, or 'xfs_bmbt_irec_t' unpacked).
 *
 * @author Luke Quinane
 */
public class DataExtent extends XfsObject {

    /**
     * The length of the structure when packed for storage on disk.
     */
    public static final int PACKED_LENGTH = 0x10;

    /**
     * The start offset.
     */
    private long startOffset;

    /**
     * The start block.
     */
    private long startBlock;

    /**
     * The block count.
     */
    private int blockCount;

    /**
     * The state.
     */
    private int state;

    /**
     * Creates a new extent from the packed on disk format.
     *
     * @param data the data to read from.
     * @param offset the offset to read from.
     */
    public DataExtent(byte[] data, int offset) {
        super(data, offset);

        blockCount = (int) (getUInt32(0xc) & 0x3ffffL);

        startBlock = ((getUInt32(0xc) & 0xfffc0000L) >> 21) | (getUInt32(0x8) << 14) | ((getUInt32(0x4) & 0x3fL) << 38);

        startOffset = ((getUInt32(0x0) & 0x7fffffff) << 22) | ((getUInt32(0x4) & 0xfffffcL) >> 9);
    }

    /**
     * Gets the start offset.
     *
     * @return the start offset.
     */
    public long getStartOffset() {
        return startOffset;
    }

    /**
     * Gets the start block for the extent.
     *
     * @return the start block.
     */
    public long getStartBlock() {
        return startBlock;
    }

    /**
     * Gets the block count.
     *
     * @return the block count.
     */
    public long getBlockCount() {
        return blockCount;
    }

    /**
     * Checks if the given file offset is within the range covered by this extent.
     *
     * @param fileOffset the file offset to check.
     * @param blockSize the file system block size.
     * @return {@code true} if this extent covers the file offset.
     */
    public boolean isWithinExtent(long fileOffset, long blockSize) {
        return
            fileOffset >= startOffset * blockSize &&
            fileOffset < (startOffset + blockCount) * blockSize;
    }

    @Override
    public String toString() {
        return String.format("extent:[start: 0x%x start-block:0x%x block-count:%d]",
            startOffset, startBlock, blockCount);
    }
}
