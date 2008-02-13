package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class Node {
	protected NodeDescriptor descriptor;
	protected int[] offsets;
	
	public Node(NodeDescriptor descriptor, byte[] nodeData, int nodeSize){
		this.descriptor = descriptor;
		offsets = new int[descriptor.getNumRecords()+1];
		for(int i = 0; i < offsets.length; ++i) {
		    offsets[i] = BigEndian.getInt16(nodeData, nodeSize-((i+1)*2));
		}
	}
	
	public NodeDescriptor getDescriptor() {
		return descriptor;
	}
	public int[] getOffsets() {
		return offsets;
	}
}
