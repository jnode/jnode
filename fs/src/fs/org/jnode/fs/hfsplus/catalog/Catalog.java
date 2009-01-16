package org.jnode.fs.hfsplus.catalog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HFSPlusParams;
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
    private HfsPlusFileSystem fs;
    private NodeDescriptor btnd;
    private BTHeaderRecord bthr;
    private int firstNodeOffset;

    /**
     * Create new Catalog
     * 
     * @param params
     */
    public Catalog(HFSPlusParams params) {
        btnd = new NodeDescriptor();
        btnd.setKind(HfsPlusConstants.BT_HEADER_NODE);
        btnd.setHeight(0);
        btnd.setRecordCount(3);
        //
        bthr = new BTHeaderRecord();
        bthr.setTreeDepth(1);
        bthr.setRootNode(1);
        bthr.settFirstLeafNode(1);
        bthr.setLastLeafNode(1);
        bthr.setLeafRecords(params.isJournaled() ? 6 : 2);
        bthr.setNodeSize(params.getCatalogNodeSize());
        bthr.setTotalNodes(params.getCatalogClumpSize() / params.getCatalogNodeSize());
        bthr.setFreeNodes(bthr.getTotalNodes() - 2);
        bthr.setClumpSize(params.getCatalogClumpSize());
        // TODO initialize attributes, max key length and key comparaison type.
        // Root directory
        CatalogKey ck = new CatalogKey(CatalogNodeId.HFSPLUS_POR_CNID, new HFSUnicodeString(params.getVolumeName()));
        CatalogFolder folder = new CatalogFolder();
        folder.setFolderId(CatalogNodeId.HFSPLUS_ROOT_CNID);
        folder.setValence(params.isJournaled() ? 2 : 0);
        // TODO creation date, content modification date, text encoding and access rights.
        ck = new CatalogKey(CatalogNodeId.HFSPLUS_ROOT_CNID, new HFSUnicodeString(""));
        CatalogThread ct = new CatalogThread(HfsPlusConstants.RECORD_TYPE_FOLDER_THREAD,
                CatalogNodeId.HFSPLUS_ROOT_CNID, new HFSUnicodeString(""));
    }

    /**
     * Create Catalog based on meta-data that exist on the file system.
     * 
     * @param fs HFS+ file system that contains catalog informations.
     * 
     * @throws IOException
     */
    public Catalog(final HfsPlusFileSystem fs) throws IOException {
        log.debug("Initialize catalog\n");
        this.fs = fs;
        Superblock sb = fs.getVolumeHeader();
        int offset = 0;
        // Get btree header record and node descriptor.
        ExtentDescriptor firstExtent = sb.getCatalogFile().getExtents()[0];
        if (firstExtent.getStartBlock() != 0 && firstExtent.getBlockCount() != 0) {
            ByteBuffer buffer = ByteBuffer.allocate(14);
            firstNodeOffset = firstExtent.getStartBlock() * sb.getBlockSize();
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
    public final LeafRecord getRecord(final CatalogNodeId parentID)
        throws IOException {
        int currentOffset = firstNodeOffset;
        int currentNodeNumber = getBTHeaderRecord().getRootNode();
        int currentNodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(currentNodeSize);
        fs.getApi().read(currentOffset + (currentNodeNumber * currentNodeSize), nodeData);
        NodeDescriptor currentBtnd = new NodeDescriptor(nodeData.array());
        log.debug("Current node descriptor:\n" + currentBtnd.toString());
        while (currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
            CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, nodeData.array(), currentNodeSize);
            IndexRecord record = currentIndexNode.find(parentID);
            currentNodeNumber = record.getIndex();
            currentOffset = firstNodeOffset + (currentNodeNumber * currentNodeSize);
            log.debug("Current node number: " + currentNodeNumber + " currentOffset:" + currentOffset + "("
                    + currentNodeSize + ")");
            nodeData = ByteBuffer.allocate(currentNodeSize);
            fs.getApi().read(currentOffset, nodeData);
            currentBtnd = new NodeDescriptor(nodeData.array());
            log.debug("Current node descriptor:\n" + currentBtnd.toString());
        }
        LeafRecord lr = null;
        if (currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
            CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, nodeData.array(), currentNodeSize);
            lr = leaf.find(parentID);
            log.debug("Leaf record :\n" + lr.toString());
        }
        return lr;
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the root node of the tree.
     *  
     * @param parentID Parent node id
     * 
     * @return Array of LeafRecord
     * 
     * @throws IOException
     */
    public final LeafRecord[] getRecords(final CatalogNodeId parentID)
        throws IOException {
        return getRecords(parentID, getBTHeaderRecord().getRootNode());
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the node correspding 
     * to the index passed as parameter.
     * 
     * @param parentID Parent node id
     * @param nodeNumber Index of node where the search begin.
     * 
     * @return Array of LeafRecord
     * 
     * @throws IOException
     */
    public final LeafRecord[] getRecords(final CatalogNodeId parentID, final int nodeNumber)
        throws IOException {
        try {
            int currentOffset = firstNodeOffset;
            int currentNodeNumber = nodeNumber;
            int currentNodeSize = getBTHeaderRecord().getNodeSize();
            ByteBuffer nodeData = ByteBuffer.allocate(currentNodeSize);
            fs.getApi().read(currentOffset + (currentNodeNumber * currentNodeSize), nodeData);
            NodeDescriptor currentBtnd = new NodeDescriptor(nodeData.array());
            log.debug("Current node descriptor:\n" + currentBtnd.toString());
            if (currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
                CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, nodeData.array(), 
                        currentNodeSize);
                IndexRecord[] records = currentIndexNode.findChilds(parentID);
                List<LeafRecord> lfList = new LinkedList<LeafRecord>();
                for (IndexRecord rec : records) {
                    LeafRecord[] lfr = getRecords(parentID, rec.getIndex());
                    for (LeafRecord lr : lfr) {
                        lfList.add(lr);
                    }
                }
                return lfList.toArray(new LeafRecord[lfList.size()]);
            } else if (currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
                CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, nodeData.array(), currentNodeSize);
                LeafRecord[] lr = leaf.findAll(parentID);
                log.debug("Leaf record size: " + lr.length);
                return lr;
            } else {
                return null;
                //
            }

        } catch (Exception e) {
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
    public final LeafRecord getRecord(final CatalogNodeId parentID, final HFSUnicodeString nodeName)
        throws IOException {
        int currentOffset = firstNodeOffset;
        int currentNodeNumber = getBTHeaderRecord().getRootNode();
        int currentNodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer buffer = ByteBuffer.allocate(currentNodeSize);
        fs.getApi().read(currentOffset + (currentNodeNumber * currentNodeSize), buffer);
        NodeDescriptor currentBtnd = new NodeDescriptor(buffer.array());
        log.debug("Current node descriptor: \n" + currentBtnd.toString());
        CatalogKey cKey = new CatalogKey(parentID, nodeName);
        while (currentBtnd.getKind() == HfsPlusConstants.BT_INDEX_NODE) {
            CatalogIndexNode currentIndexNode = new CatalogIndexNode(currentBtnd, buffer.array(), currentNodeSize);
            IndexRecord record = currentIndexNode.find(cKey);
            currentNodeNumber = record.getIndex();
            currentOffset = currentNodeNumber * currentNodeSize;
            buffer = ByteBuffer.allocate(currentNodeSize);
            fs.getApi().read(currentOffset, buffer);
            currentBtnd = new NodeDescriptor(buffer.array());
        }
        LeafRecord lr = null;
        if (currentBtnd.getKind() == HfsPlusConstants.BT_LEAF_NODE) {
            CatalogLeafNode leaf = new CatalogLeafNode(currentBtnd, buffer.array(), currentNodeSize);
            lr = leaf.find(parentID);
            log.debug("Leaf record: \n" + lr.toString());
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
