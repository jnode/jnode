package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class ExtentLeafNode extends LeafNode {
	public ExtentLeafNode(NodeDescriptor descriptor, byte[] nodeData, int nodeSize){
		super(descriptor, nodeData, nodeSize);
		for(int i = 0; i < records.length; ++i) {
		    int currentOffset = offsets[i];
		    Key currentKey = new ExtentKey(nodeData, currentOffset);
		    records[i] = new LeafRecord(currentKey, nodeData, currentOffset);
		}
	}
}
