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
    
    public int getSize(){
        return key.getKeyLength() + recordData.length;
    }
    
    public abstract byte[] getBytes();
}
