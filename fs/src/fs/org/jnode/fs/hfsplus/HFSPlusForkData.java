package org.jnode.fs.hfsplus;

import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.util.BigEndian;

public class HFSPlusForkData {
    public static final int FORK_DATA_LENGTH = 80;
    private static final int EXTENT_OFFSET = 16;

    private byte[] data;

    public HFSPlusForkData(final byte[] src, final int offset) {
        data = new byte[FORK_DATA_LENGTH];
        System.arraycopy(src, offset, data, 0, FORK_DATA_LENGTH);
    }

    public final long getTotalSize() {
        return BigEndian.getInt64(data, 0);
    }

    public final int getClumpSize() {
        return BigEndian.getInt32(data, 8);
    }

    public final int getTotalBlocks() {
        return BigEndian.getInt32(data, 12);
    }

    public final ExtentDescriptor[] getExtents() {
        ExtentDescriptor[] list = new ExtentDescriptor[8];
        for (int i = 0; i < 8; i++) {
            list[i] = new ExtentDescriptor(
                    data, EXTENT_OFFSET + (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
        return list;
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Total size : ").append(getTotalSize()).append("\n");
        s.append("Clump size : ").append(getClumpSize()).append("\n");
        s.append("Total Blocks : ").append(getTotalBlocks()).append("\n");
        ExtentDescriptor[] list = getExtents();
        for (int i = 0; i < list.length; i++) {
            s.append("Extent[" + i + "]: " + list[i].toString());
        }
        return s.toString();
    }
}
