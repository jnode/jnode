package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class Node {
    protected NodeDescriptor descriptor;
    protected byte[] nodeData;
    protected int nodeSize;

    public Node(NodeDescriptor descriptor, final int nodeSize) {
        this.descriptor = descriptor;
        this.nodeData = new byte[nodeSize];
        this.nodeSize = nodeSize;
    }
    
    public Node(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        this.descriptor = descriptor;
        this.nodeData = nodeData;
        this.nodeSize = nodeSize;
    }

    public NodeDescriptor getDescriptor() {
        return descriptor;
    }
    
    public int getOffset(int index) {
        return BigEndian.getInt16(nodeData, nodeSize - ((index + 1) * 2));
    }
    
    public void setOffset(int index, int offsetValue) {
        BigEndian.setInt16(nodeData, nodeSize - ((index + 1) * 2), offsetValue);
    }

}
