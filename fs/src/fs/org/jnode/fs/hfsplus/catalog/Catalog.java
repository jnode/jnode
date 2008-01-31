package org.jnode.fs.hfsplus.catalog;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.Superblock;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.tree.BTHeaderRecord;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.fs.spi.AbstractFileSystem;

public class Catalog {
	private final Logger log = Logger.getLogger(getClass());
	/** */
	private AbstractFileSystem<?> fs;
	/** */
	private Superblock header;
	/** */
	private NodeDescriptor btnd;
	/** */
	private BTHeaderRecord bthr;
	
	public Catalog(Superblock header, HfsPlusFileSystem fs){
		this.header = header;
		this.fs = fs;
	}
	
	public int init() throws IOException {
		int offset = 0;
		ExtentDescriptor current = header.getCatalogFile().getExtents()[0];
		if(current.getStartBlock() != 0 && current.getBlockCount() != 0){
			ByteBuffer buffer = ByteBuffer.allocate(14);
			offset = current.getStartBlock()*header.getBlockSize();
			fs.getApi().read(offset, buffer);
			btnd = new NodeDescriptor(buffer.array());
			log.debug("BTNodeDescriptor informations :\n" + btnd.toString());
			offset = offset + 14;
			buffer = ByteBuffer.allocate(106);
			fs.getApi().read(offset, buffer);
			bthr = new BTHeaderRecord(buffer.array());
			log.debug("BTHeaderRec informations :\n" + bthr.toString());
			offset = offset + 106;
			offset = current.getStartBlock()*header.getBlockSize();
		}
		return offset;
	}

	public NodeDescriptor getBTNodeDescriptor() {
		return btnd;
	}

	public BTHeaderRecord getBTHeaderRecord() {
		return bthr;
	}
	
	public LeafRecord getRecord(CatalogNodeId parentID, int currentOffset) throws IOException{
		int currentNodeNumber = getBTHeaderRecord().getRootNode();
		int currentNodeSize = getBTHeaderRecord().getNodeSize();
		ByteBuffer buffer = ByteBuffer.allocate(currentNodeSize);
		fs.getApi().read(currentOffset + (currentNodeNumber*currentNodeSize), buffer);
		NodeDescriptor currentBtnd = new NodeDescriptor(buffer.array());
		log.debug("Current node descriptor :\n" + currentBtnd.toString());
		while(currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
			CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, buffer.array(), currentNodeSize);
			IndexRecord record = currentIndexNode.find(parentID);
			currentNodeNumber = record.getIndex();
			currentOffset = currentNodeNumber*currentNodeSize;
			buffer = ByteBuffer.allocate(currentNodeSize);
			fs.getApi().read(currentOffset, buffer);
			currentBtnd = new NodeDescriptor(buffer.array());
		}
		if(currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
			CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, buffer.array(), currentNodeSize);
		    LeafRecord lr = leaf.find(parentID);
		    log.debug("Leaf record :\n" + lr.toString());
		    return lr;
		}
		return null;
	}

}
