package org.jnode.fs.hfsplus.extent;

import org.jnode.util.BigEndian;

public class ExtentDescriptor {

    public static final int EXTENT_DESCRIPTOR_LENGTH = 8;

    private byte[] data;

    /**
     * Create empty extent descriptor.
     */
    public ExtentDescriptor() {
        data = new byte[EXTENT_DESCRIPTOR_LENGTH];
    }

    public ExtentDescriptor(final byte[] src, final int offset) {
        data = new byte[EXTENT_DESCRIPTOR_LENGTH];
        System.arraycopy(src, offset, data, 0, EXTENT_DESCRIPTOR_LENGTH);
    }

    public final int getStartBlock() {
        return BigEndian.getInt32(data, 0);
    }

    public final void setStartBlock(int start) {
        BigEndian.setInt32(data, 0, start);
    }

    public final int getBlockCount() {
        return BigEndian.getInt32(data, 4);
    }

    public final void setBlockCount(int count) {
        BigEndian.setInt32(data, 4, count);
    }

    public final byte[] getBytes() {
        return data;
    }

    public final String toString() {
        return "Start block : " + getStartBlock() + "\tBlock count : " + getBlockCount() + "\n";
    }
}
