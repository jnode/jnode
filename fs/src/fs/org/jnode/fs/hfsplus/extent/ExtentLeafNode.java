package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class ExtentLeafNode extends LeafNode {
    
    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public ExtentLeafNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        for (int i = 0; i < records.length; ++i) {
            int currentOffset = getOffset(i);
            int recordDataSize = getOffset(i + 1) - currentOffset;
            Key currentKey = new ExtentKey(nodeData, currentOffset);
            records[i] = new LeafRecord(currentKey, nodeData, currentOffset, recordDataSize);
        }
    }
}
