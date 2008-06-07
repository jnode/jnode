package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class BTHeaderRecord {
    public static final int BT_HEADER_RECORD_LENGTH = 106;
    private byte[] data;

    public BTHeaderRecord(final byte[] src) {
        data = new byte[BT_HEADER_RECORD_LENGTH];
        System.arraycopy(src, 0, data, 0, BT_HEADER_RECORD_LENGTH);
    }

    public final int getTreeDepth() {
        return BigEndian.getInt16(data, 0);
    }

    public final int getRootNode() {
        return BigEndian.getInt32(data, 2);
    }

    public final int getLeafRecords() {
        return BigEndian.getInt32(data, 6);
    }

    public final int getFirstLeafNode() {
        return BigEndian.getInt32(data, 10);
    }

    public final int getLastLeafNode() {
        return BigEndian.getInt32(data, 14);
    }

    public final int getNodeSize() {
        return BigEndian.getInt16(data, 18);
    }

    public final String toString() {
        return ("Root node:  " + getRootNode() + "\n" +
                "First leaf: " + getFirstLeafNode() + "\n" +
                "Last leaf:  " + getLastLeafNode() + "\n" +
                "node size:  " + getNodeSize() + "\n");
    }
}
