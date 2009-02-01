package org.jnode.fs.hfsplus.tree;

public abstract class AbstractNodeRecord implements NodeRecord {

    protected Key key = null;
    protected byte[] recordData = null;

    public Key getKey() {
        return key;
    }

    public byte[] getData() {
        return recordData;
    }

    public int getSize() {
        return key.getKeyLength() + recordData.length;
    }

    public byte[] getBytes() {
        byte[] data = new byte[key.getKeyLength() + this.recordData.length];
        System.arraycopy(data, 0, key.getBytes(), 0, key.getKeyLength());
        System.arraycopy(data, key.getKeyLength(), this.recordData, 0, this.recordData.length);
        return data;
    }
}
