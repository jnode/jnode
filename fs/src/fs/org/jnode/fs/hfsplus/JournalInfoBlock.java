package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;

public class JournalInfoBlock {
    private byte[] data;

    public JournalInfoBlock(final byte[] src) {
        data = new byte[180];
        System.arraycopy(src, 0, data, 0, 180);
    }

    public final int getFlag() {
        return BigEndian.getInt32(data, 0);
    }

    public final long getOffset() {
        return BigEndian.getInt64(data, 36);
    }

    public final long getSize() {
        return BigEndian.getInt64(data, 44);
    }

    public final String toString() {
        return "Journal : " + getOffset() + "::" + getSize();
    }
}
