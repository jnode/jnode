package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class LeafRecord extends AbstractNodeRecord {

    public LeafRecord(final Key key, final byte[] recordData) {
        this.key = key;
        this.recordData = new byte[recordData.length];
        System.arraycopy(recordData, 0, this.recordData, 0, recordData.length);
    }

    public LeafRecord(final Key key, final byte[] nodeData, final int offset, final int recordDataSize) {
        this.key = key;
        this.recordData = new byte[recordDataSize];
        System.arraycopy(nodeData, offset + key.getKeyLength() + 2, this.recordData, 0, recordDataSize);
    }

    public final int getType() {
        return BigEndian.getInt16(this.recordData, 0);
    }

    @Override
    public byte[] getBytes() {
        byte[] data = new byte[key.getKeyLength() + this.recordData.length];
        System.arraycopy(data, 0, key.getBytes(), 0, key.getKeyLength());
        System.arraycopy(data, key.getKeyLength(), this.recordData, 0, this.recordData.length);
        return data;
    }

    public final String toString() {
        return "Type : " + getType() + "\nKey : " + getKey().toString() + "\n";
    }

}
