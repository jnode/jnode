package org.jnode.fs.ext2;

import java.io.IOException;

/**
 * An ext4 extent header.
 *
 * @author Luke Quinane
 */
public class ExtentHeader {
    /**
     * The length of an extent header.
     */
    public static final int EXTENT_HEADER_LENGTH = 12;

    /**
     * The magic number for an extent header.
     */
    public static final int MAGIC = 0xf30a;

    /**
     * The data for the header.
     */
    private byte[] data;

    /**
     * The cache copy of the index entries.
     */
    private ExtentIndex[] indexEntries;

    /**
     * The cache copy of the extent entries.
     */
    private Extent[] extentEntries;

    /**
     * Create an extent header object.
     */
    public ExtentHeader(byte[] data) throws IOException {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        if (getMagic() != ExtentHeader.MAGIC) {
            throw new IOException("Extent had the wrong magic: " + getMagic());
        }
    }

    public int getMagic() {
        return Ext2Utils.get16(data, 0);
    }

    public int getEntryCount() {
        return Ext2Utils.get16(data, 2);
    }

    public int getMaximumEntryCount() {
        return Ext2Utils.get16(data, 4);
    }

    public int getDepth() {
        return Ext2Utils.get16(data, 6);
    }

    public ExtentIndex[] getIndexEntries() {
        if (getDepth() == 0) {
            throw new IllegalStateException("Trying to read index entries from a leaf.");
        }

        if (indexEntries == null) {
            indexEntries = new ExtentIndex[getEntryCount()];
            int offset = EXTENT_HEADER_LENGTH;

            for (int i = 0; i < getEntryCount(); i++) {
                byte[] indexBuffer = new byte[ExtentIndex.EXTENT_INDEX_LENGTH];
                System.arraycopy(data, offset, indexBuffer, 0, indexBuffer.length);

                indexEntries[i] = new ExtentIndex(indexBuffer);
                offset += ExtentIndex.EXTENT_INDEX_LENGTH;
            }
        }

        return indexEntries;
    }

    public Extent[] getExtentEntries() {
        if (getDepth() != 0) {
            throw new IllegalStateException("Trying to read extent entries from a non-leaf.");
        }

        if (extentEntries == null) {
            extentEntries = new Extent[getEntryCount()];
            int offset = EXTENT_HEADER_LENGTH;

            for (int i = 0; i < getEntryCount(); i++) {
                byte[] indexBuffer = new byte[Extent.EXTENT_LENGTH];
                System.arraycopy(data, offset, indexBuffer, 0, indexBuffer.length);

                extentEntries[i] = new Extent(indexBuffer);
                offset += Extent.EXTENT_LENGTH;
            }
        }

        return extentEntries;
    }

    public long getBlockNumber(Ext2FileSystem fs, long index) throws IOException {
        if (getDepth() > 0) {
            ExtentIndex extentIndex = binarySearchIndexes(index, getIndexEntries());
            byte[] indexData = fs.getBlock(extentIndex.getLeafLow());

            ExtentHeader indexHeader = new ExtentHeader(indexData);
            return indexHeader.getBlockNumber(fs, index);
        }
        else {
            Extent extent = binarySearchExtents(index, getExtentEntries());
            return index - extent.getBlockIndex() + extent.getStartLow();
        }
    }

    /**
     * Performs a binary search in the extent indexes.
     *
     * @param index the index of the block to match.
     * @param indexes the indexes to search in.
     * @return the matching index.
     */
    private ExtentIndex binarySearchIndexes(long index, ExtentIndex[] indexes)
    {
        int lowIndex = 0;
        int highIndex = indexes.length - 1;
        ExtentIndex extentIndex = null;

        while (lowIndex <= highIndex) {
            int middle = lowIndex + (highIndex - lowIndex) / 2;
            extentIndex = indexes[middle];

            if (index < extentIndex.getBlockIndex()) {
                highIndex = middle - 1;
            }
            else {
                lowIndex = middle + 1;
            }
        }

        return indexes[Math.max(0, lowIndex - 1)];
    }

    /**
     * Performs a binary search in the extents.
     *
     * @param index the index of the block to match.
     * @param extents the extents to search in.
     * @return the matching extent.
     */
    private Extent binarySearchExtents(long index, Extent[] extents)
    {
        int lowIndex = 0;
        int highIndex = extents.length - 1;
        Extent extent = null;

        while (lowIndex <= highIndex) {
            int middle = lowIndex + (highIndex - lowIndex) / 2;
            extent = extents[middle];

            if (index < extent.getBlockIndex()) {
                highIndex = middle - 1;
            }
            else {
                lowIndex = middle + 1;
            }
        }

        return extents[Math.max(0, lowIndex - 1)];
    }

    @Override
    public String toString() {
        return String.format("ExtentHeader: depth:%d entries:%d/%d", getDepth(), getEntryCount(), getMaximumEntryCount());
    }
}
