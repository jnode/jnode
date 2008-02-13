package org.jnode.fs.hfsplus.tree;

public class IndexNode extends Node {
	protected IndexRecord[] records;
	/**
	 * 
	 * @param descriptor
	 * @param nodeData
	 * @param nodeSize
	 */
	public IndexNode(NodeDescriptor descriptor, byte[] nodeData, int nodeSize){
		super(descriptor, nodeData, nodeSize);
		records = new IndexRecord[offsets.length-1];
	}
	/**
	 * 
	 * @return
	 */
	public IndexRecord[] getRecords() {
		return records;
	}
}
