package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class IndexRecord extends AbstractNodeRecord {

    /**
     * 
     * @param key
     * @param nodeData
     * @param offset
     */
    public IndexRecord(final Key key, final byte[] nodeData, final int offset) {
        this.key = key;
        this.recordData = new byte[4];
        System.arraycopy(nodeData, offset + key.getKeyLength(), recordData, 0, 4);
    }
   
    public final int getIndex() {
        return BigEndian.getInt32(recordData, 0);
    }
    
}
