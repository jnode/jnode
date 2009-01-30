package org.jnode.fs.hfsplus.tree;



public interface Node {
    public NodeDescriptor getNodeDescriptor();
    public boolean isIndexNode();
    public boolean isLeafNode();
    public int getRecordOffset(int index);
    public NodeRecord getNodeRecord(int index);
    public void addNodeRecord(int index, NodeRecord record, int offset);
}
