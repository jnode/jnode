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

    public long getBlockNumber(long index) {
        if (getDepth() > 0) {
            ExtentIndex[] indexes = getIndexEntries();

            throw new UnsupportedOperationException();
        }
        else {
            Extent[] extents = getExtentEntries();

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

            return index - extent.getBlockIndex() + extent.getStartLow();
        }
    }

    @Override
    public String toString() {
        return String.format("ExtentHeader: depth:%d entries:%d/%d", getDepth(), getEntryCount(), getMaximumEntryCount());
    }
}
