package org.jnode.fs.hfsplus.catalog;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HFSUnicodeString;
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
	private NodeDescriptor btnd;
	/** */
	private BTHeaderRecord bthr;
	/** */
	private int firstNodeOffset;
	
	public Catalog(HfsPlusFileSystem fs) throws IOException {
		this.fs = fs;
		Superblock sb = fs.getSb();
		int offset = 0;
		ExtentDescriptor current = sb.getCatalogFile().getExtents()[0];
		if(current.getStartBlock() != 0 && current.getBlockCount() != 0){
			ByteBuffer buffer = ByteBuffer.allocate(14);
			firstNodeOffset = current.getStartBlock()*sb.getBlockSize();
			fs.getApi().read(offset, buffer);
			btnd = new NodeDescriptor(buffer.array());
			log.debug("BTNodeDescriptor informations :\n" + btnd.toString());
			offset = firstNodeOffset + 14;
			buffer = ByteBuffer.allocate(106);
			fs.getApi().read(offset, buffer);
			bthr = new BTHeaderRecord(buffer.array());
			log.debug("BTHeaderRec informations :\n" + bthr.toString());
			offset = offset + 106;
		}
	}
	/**
	 * 
	 * @param parentID
	 * @return
	 * @throws IOException
	 */
	public LeafRecord getRecord(CatalogNodeId parentID) throws IOException{
		int currentOffset = firstNodeOffset;
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
	/**
	 * 
	 * @param parentID
	 * @return
	 * @throws IOException
	 */
	public LeafRecord[] getRecords(CatalogNodeId parentID) throws IOException{
		int currentOffset = firstNodeOffset;
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
		    LeafRecord[] lr = leaf.findAll(parentID);
		    log.debug("Leaf record size: " + lr.length);
		    return lr;
		}
		return null;
	}
	/**
	 * 
	 * @param parentID
	 * @param nodeName
	 * @return
	 * @throws IOException
	 */
	public LeafRecord getRecord(CatalogNodeId parentID, HFSUnicodeString nodeName) throws IOException{
		int currentOffset = firstNodeOffset;
		int currentNodeNumber = getBTHeaderRecord().getRootNode();
		int currentNodeSize = getBTHeaderRecord().getNodeSize();
		ByteBuffer buffer = ByteBuffer.allocate(currentNodeSize);
		fs.getApi().read(currentOffset + (currentNodeNumber*currentNodeSize), buffer);
		NodeDescriptor currentBtnd = new NodeDescriptor(buffer.array());
		log.debug("Current node descriptor :\n" + currentBtnd.toString());
		while(currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
			CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, buffer.array(), currentNodeSize);
			IndexRecord record = currentIndexNode.find(new CatalogKey(parentID, nodeName));
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
	
	public NodeDescriptor getBTNodeDescriptor() {
		return btnd;
	}

	public BTHeaderRecord getBTHeaderRecord() {
		return bthr;
	}

}
