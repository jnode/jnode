package org.jnode.fs.hfsplus.tree;

import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.util.BigEndian;

public abstract class AbstractNode implements Node {
    
    protected byte[] datas;
    protected int size;

    @Override
    public NodeDescriptor getNodeDescriptor() {
        return new NodeDescriptor(datas, 0);
    }
    
    public boolean isIndexNode(){
        return this.getNodeDescriptor().getKind() == HfsPlusConstants.BT_INDEX_NODE;
    }
    
    public boolean isLeafNode(){
        return this.getNodeDescriptor().getKind() == HfsPlusConstants.BT_LEAF_NODE;
    }
    
    @Override
    public int getRecordOffset(int index) {
        return BigEndian.getInt16(datas, size - ((index + 1) * 2));
    }

    @Override
    public abstract NodeRecord getNodeRecord(int index);
  
    @Override
    public void addNodeRecord(int index, NodeRecord record, int offset) {
        BigEndian.setInt16(datas, size - ((index + 1) * 2), offset);
        System.arraycopy(record.getBytes(), 0, datas, offset, record.getSize());
    }
    
    public byte[] getBytes(){
        return datas;
    }
}
