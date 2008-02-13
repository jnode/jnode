package org.jnode.fs.hfsplus.tree;

public class LeafNode extends Node {
	protected LeafRecord[] records;
	/**
	 * 
	 * @param descriptor
	 * @param nodeData
	 * @param nodeSize
	 */
	public LeafNode(NodeDescriptor descriptor, byte[] nodeData, int nodeSize){
		super(descriptor, nodeData, nodeSize);
		records = new LeafRecord[offsets.length-1];
	}
	/**
	 * 
	 * @return
	 */
	public LeafRecord[] getRecords() {
		return records;
	}
}
