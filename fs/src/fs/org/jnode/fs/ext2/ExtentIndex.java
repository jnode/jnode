package org.jnode.fs.ext2;

/**
 * An ext4 extent index.
 *
 * @author Luke Quinane
 */
public class ExtentIndex {
    /**
     * The length of an extent index.
     */
    public static final int EXTENT_INDEX_LENGTH = 12;

    /**
     * The data for the index.
     */
    private byte[] data;

    /**
     * Create an extent index object.
     *
     * @param data the data for the index.
     */
    public ExtentIndex(byte[] data) {
        this.data = new byte[EXTENT_INDEX_LENGTH];
        System.arraycopy(data, 0, this.data, 0, EXTENT_INDEX_LENGTH);

        // Safety check
        if (getLeafHigh() != 0) {
            throw new UnsupportedOperationException("Extent indexes that use the high bits aren't supported yet");
        }
    }

    public long getBlockIndex() {
        return Ext2Utils.get32(data, 0);
    }

    public long getLeafLow() {
        return Ext2Utils.get32(data, 4);
    }

    public int getLeafHigh() {
        return Ext2Utils.get16(data, 8);
    }

    @Override
    public String toString() {
        return String.format("ExtentIndex: blockindex:%d leaf(low:%d high:%d)", getBlockIndex(), getLeafLow(),
                             getLeafHigh());
    }
}
