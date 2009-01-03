package org.jnode.fs.hfsplus.tree;

public class LeafNode extends Node {
    protected LeafRecord[] records;

    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public LeafNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        records = new LeafRecord[offsets.length - 1];
    }

   
}
