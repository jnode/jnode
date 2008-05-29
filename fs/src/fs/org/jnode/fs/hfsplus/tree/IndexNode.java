package org.jnode.fs.hfsplus.tree;

public class IndexNode extends Node {
	protected IndexRecord[] records;
	/**
	 * 
	 * @param descriptor
	 * @param nodeData
	 * @param nodeSize
	 */
	public IndexNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize){
		super(descriptor, nodeData, nodeSize);
		records = new IndexRecord[offsets.length-1];
	}
	/**
	 * 
	 * @return
	 */
	public final IndexRecord[] getRecords() {
		return records;
	}
}
