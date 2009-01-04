package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class NodeDescriptor {
    public static final int BT_NODE_DESCRIPTOR_LENGTH = 14;
    private byte[] data;

    public NodeDescriptor(final byte[] src) {
        data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
        System.arraycopy(src, 0, data, 0, BT_NODE_DESCRIPTOR_LENGTH);
    }

    public final int getFLink() {
        return BigEndian.getInt32(data, 0);
    }

    public final int getBLink() {
        return BigEndian.getInt32(data, 4);
    }

    public final int getKind() {
        return BigEndian.getInt8(data, 8);
    }

    public final int getHeight() {
        return BigEndian.getInt8(data, 9);
    }

    public final int getNumRecords() {
        return BigEndian.getInt16(data, 10);
    }

    public final String toString() {
        return ("FLink:  " + getFLink() + "\n" + "BLink:  " + getBLink() + "\n" + "Kind:   " + getKind() + "\n"
                + "height: " + getHeight() + "\n" + "#rec:   " + getNumRecords() + "\n");
    }
}
