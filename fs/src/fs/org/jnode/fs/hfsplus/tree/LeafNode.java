package org.jnode.fs.hfsplus.tree;

public class LeafNode extends Node {
    protected LeafRecord[] records;

    /**
     * 
     * @param descriptor
     * @param nodeSize
     */
    public LeafNode(final NodeDescriptor descriptor, int nodeSize) {
        super(descriptor, nodeSize);
        records = new LeafRecord[descriptor.getNumRecords()];
    }

    /**
     * 
     * @param record
     * @param offset
     * @param index
     */
    public void addRecord(LeafRecord record, int offset, int index) {
        records[index] = record;
        this.setOffset(index, offset);
    }
    
    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public LeafNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        records = new LeafRecord[descriptor.getNumRecords()];
    }

}
