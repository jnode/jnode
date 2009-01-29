package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class NodeDescriptor {
    public static final int BT_NODE_DESCRIPTOR_LENGTH = 14;
    private byte[] data;

    public NodeDescriptor() {
        data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
    }

    public NodeDescriptor(final byte[] src, int offset) {
        data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
        System.arraycopy(src, offset, data, 0, BT_NODE_DESCRIPTOR_LENGTH);
    }

    public final int getFLink() {
        return BigEndian.getInt32(data, 0);
    }
    
    public void setFLink(int link) {
        BigEndian.setInt32(data, 0, link);
    }

    public final int getBLink() {
        return BigEndian.getInt32(data, 4);
    }
    
    public void setBLink(int link) {
        BigEndian.setInt32(data, 4, link);
    }

    public final int getKind() {
        return BigEndian.getInt8(data, 8);
    }

    public void setKind(int kind) {
        BigEndian.setInt8(data, 8, kind);
    }

    public final int getHeight() {
        return BigEndian.getInt8(data, 9);
    }

    public void setHeight(int height) {
        BigEndian.setInt8(data, 9, height);
    }

    public final int getNumRecords() {
        return BigEndian.getInt16(data, 10);
    }

    public void setRecordCount(int count) {
        BigEndian.setInt16(data, 10, count);
    }

    public byte[] getBytes() {
        return data;
    }
    
    public final String toString() {
        return ("FLink:  " + getFLink() + "\n" + "BLink:  " + getBLink() + "\n" + "Kind:   " + getKind() + "\n"
                + "height: " + getHeight() + "\n" + "#rec:   " + getNumRecords() + "\n");
    }
}
