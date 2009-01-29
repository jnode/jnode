package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.tree.IndexNode;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class ExtentIndexNode extends IndexNode {
    
    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public ExtentIndexNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        for (int i = 0; i < records.length; ++i) {
            int currentOffset = getOffset(i);
            Key currentKey = new ExtentKey(nodeData, currentOffset);
            records[i] = new IndexRecord(currentKey, nodeData, currentOffset);
        }
    }
}
