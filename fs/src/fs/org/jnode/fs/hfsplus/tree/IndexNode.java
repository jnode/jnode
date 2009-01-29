package org.jnode.fs.hfsplus.tree;

public class IndexNode extends Node {
    protected IndexRecord[] records;

    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public IndexNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        records = new IndexRecord[descriptor.getNumRecords()];
    }

    /**
     * 
     * @return
     */
    public final IndexRecord getRecord(int index) {
        return records[index];
    }
}
