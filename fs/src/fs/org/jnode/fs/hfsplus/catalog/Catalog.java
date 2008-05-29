package org.jnode.fs.hfsplus.catalog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

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

public class Catalog {
	private final Logger log = Logger.getLogger(getClass());
	/** */
	private HfsPlusFileSystem fs;
	/** */
	private NodeDescriptor btnd;
	/** */
	private BTHeaderRecord bthr;
	/** */
	private int firstNodeOffset;
	
	public Catalog(final HfsPlusFileSystem fs) throws IOException {
		log.debug("Initialize catalog\n");
		this.fs = fs;
		Superblock sb = fs.getVolumeHeader();
		int offset = 0;
		// Get btree header record and node descriptor.
		ExtentDescriptor firstExtent = sb.getCatalogFile().getExtents()[0];
		if(firstExtent.getStartBlock() != 0 && firstExtent.getBlockCount() != 0){
			ByteBuffer buffer = ByteBuffer.allocate(14);
			firstNodeOffset = firstExtent.getStartBlock()*sb.getBlockSize();
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
	public final LeafRecord getRecord(final CatalogNodeId parentID) throws IOException{
		int currentOffset = firstNodeOffset;
		int currentNodeNumber = getBTHeaderRecord().getRootNode();
		int currentNodeSize = getBTHeaderRecord().getNodeSize();
		ByteBuffer nodeData = ByteBuffer.allocate(currentNodeSize);
		fs.getApi().read(currentOffset + (currentNodeNumber*currentNodeSize), nodeData);
		NodeDescriptor currentBtnd = new NodeDescriptor(nodeData.array());
		log.debug("Current node descriptor :\n" + currentBtnd.toString());
		while(currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
			CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, nodeData.array(), currentNodeSize);
			IndexRecord record = currentIndexNode.find(parentID);
			currentNodeNumber = record.getIndex();
			currentOffset = firstNodeOffset + (currentNodeNumber*currentNodeSize);
			log.debug("Current node number: " + currentNodeNumber + " currentOffset:" + currentOffset + "(" + currentNodeSize + ")");
			nodeData = ByteBuffer.allocate(currentNodeSize);
			fs.getApi().read(currentOffset, nodeData);
			currentBtnd = new NodeDescriptor(nodeData.array());
			log.debug("Current node descriptor :\n" + currentBtnd.toString());
		}
		LeafRecord lr = null;
		if(currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
			CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, nodeData.array(), currentNodeSize);
		    lr = leaf.find(parentID);
		    log.debug("Leaf record :\n" + lr.toString());
		}
		return lr;
	}
	/**
	 * 
	 * @param parentID
	 * @return
	 * @throws IOException
	 */
	public final LeafRecord[] getRecords(final CatalogNodeId parentID) throws IOException {
		return getRecords(parentID,  getBTHeaderRecord().getRootNode());
	}
	/**
	 * 
	 * @param parentID
	 * @param nodeNumber
	 * @return
	 * @throws IOException
	 */
	public final LeafRecord[] getRecords(final CatalogNodeId parentID, final int nodeNumber) throws IOException {
		try {
			int currentOffset = firstNodeOffset;
			int currentNodeNumber = nodeNumber;
			int currentNodeSize = getBTHeaderRecord().getNodeSize();
			ByteBuffer nodeData = ByteBuffer.allocate(currentNodeSize);
			fs.getApi().read(currentOffset + (currentNodeNumber*currentNodeSize), nodeData);
			NodeDescriptor currentBtnd = new NodeDescriptor(nodeData.array());
			log.debug("Current node descriptor :\n" + currentBtnd.toString());
			if(currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
				CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, nodeData.array(), currentNodeSize);
				IndexRecord[] records = currentIndexNode.findChilds(parentID);
				List<LeafRecord> lfList = new LinkedList<LeafRecord>();
				for(IndexRecord rec : records) {
					LeafRecord[] lfr =  getRecords(parentID, rec.getIndex());
					for(LeafRecord lr: lfr){
						lfList.add(lr);
					}
				}
				return lfList.toArray(new LeafRecord[lfList.size()]);
			} else if(currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
				CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, nodeData.array(), currentNodeSize);
				LeafRecord[] lr = leaf.findAll(parentID);
				log.debug("Leaf record size: " + lr.length);
				return lr;
			} else {
				return null;
				//
			}

		} catch (Exception e){
			e.printStackTrace();
			throw new IOException(e);
		}
	}
	/**
	 * 
	 * @param parentID
	 * @param nodeName
	 * @return
	 * @throws IOException
	 */
	public final LeafRecord getRecord(final CatalogNodeId parentID, final HFSUnicodeString nodeName) throws IOException{
		int currentOffset = firstNodeOffset;
		int currentNodeNumber = getBTHeaderRecord().getRootNode();
		int currentNodeSize = getBTHeaderRecord().getNodeSize();
		ByteBuffer buffer = ByteBuffer.allocate(currentNodeSize);
		fs.getApi().read(currentOffset + (currentNodeNumber*currentNodeSize), buffer);
		NodeDescriptor currentBtnd = new NodeDescriptor(buffer.array());
		log.debug("Current node descriptor :\n" + currentBtnd.toString());
		CatalogKey cKey= new CatalogKey(parentID, nodeName);
		while(currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
			CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, buffer.array(), currentNodeSize);
			IndexRecord record = currentIndexNode.find(cKey);
			currentNodeNumber = record.getIndex();
			currentOffset = currentNodeNumber*currentNodeSize;
			buffer = ByteBuffer.allocate(currentNodeSize);
			fs.getApi().read(currentOffset, buffer);
			currentBtnd = new NodeDescriptor(buffer.array());
		}
		LeafRecord lr = null;
		if(currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
			CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, buffer.array(), currentNodeSize);
		    lr = leaf.find(parentID);
		    log.debug("Leaf record :\n" + lr.toString());
		}
		return lr;
	}
	
	public final NodeDescriptor getBTNodeDescriptor() {
		return btnd;
	}

	public final BTHeaderRecord getBTHeaderRecord() {
		return bthr;
	}

}
