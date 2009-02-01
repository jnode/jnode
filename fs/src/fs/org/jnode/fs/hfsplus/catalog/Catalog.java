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
import org.jnode.util.ByteBufferUtils;

public class Catalog {

    private final Logger log = Logger.getLogger(getClass());
    private HfsPlusFileSystem fs;
    /**
     * B-Tree node descriptor
     */
    private NodeDescriptor btnd;
    /**
     * B-Tree Header record
     */
    private BTHeaderRecord bthr;
    /** */
    private int catalogHeaderNodeOffset;
    /** */
    private ByteBuffer buffer;

    /**
     * Create Catalog based on meta-data that exist on the file system.
     *
     * @param fs HFS+ file system that contains catalog informations.
     * @throws IOException
     */
    public Catalog(final HfsPlusFileSystem fs) throws IOException {
        log.debug("Load B-Tree catalog file.\n");
        this.fs = fs;
        Superblock sb = fs.getVolumeHeader();
        ExtentDescriptor firstExtent = sb.getCatalogFile().getExtents()[0];
        catalogHeaderNodeOffset = firstExtent.getStartBlock() * sb.getBlockSize();
        if (firstExtent.getStartBlock() != 0 && firstExtent.getBlockCount() != 0) {
            buffer = ByteBuffer.allocate(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH
                + BTHeaderRecord.BT_HEADER_RECORD_LENGTH);
            fs.getApi().read(catalogHeaderNodeOffset, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            btnd = new NodeDescriptor(data, 0);
            bthr = new BTHeaderRecord(data, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);

        }
    }

    /**
     * Create new Catalog
     *
     * @param params
     */
    public Catalog(HFSPlusParams params) {
        log.debug("Create B-Tree catalog file.\n");

        int nodeSize = params.getCatalogNodeSize();

        int bufferLength = 0;
        btnd = new NodeDescriptor();
        btnd.setKind(HfsPlusConstants.BT_HEADER_NODE);
        btnd.setHeight(0);
        btnd.setRecordCount(3);
        bufferLength += NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH;
        //
        bthr = new BTHeaderRecord();
        bthr.setTreeDepth(1);
        bthr.setRootNode(1);
        bthr.settFirstLeafNode(1);
        bthr.setLastLeafNode(1);
        bthr.setLeafRecords(params.isJournaled() ? 6 : 2);
        bthr.setNodeSize(nodeSize);
        bthr.setTotalNodes(params.getCatalogClumpSize() / params.getCatalogNodeSize());
        bthr.setFreeNodes(bthr.getTotalNodes() - 2);
        bthr.setClumpSize(params.getCatalogClumpSize());
        // TODO initialize attributes, max key length and key comparaison type.
        bufferLength += BTHeaderRecord.BT_HEADER_RECORD_LENGTH;
        // Create root node 
        int rootNodePosition = bthr.getRootNode() * nodeSize;
        bufferLength += (rootNodePosition - bufferLength);
        //Create node descriptor
        NodeDescriptor nd = new NodeDescriptor();
        nd.setKind(HfsPlusConstants.BT_LEAF_NODE);
        nd.setHeight(1);
        nd.setRecordCount(params.isJournaled() ? 6 : 2);
        CatalogNode rootNode = new CatalogNode(nd, nodeSize);
        int offset = NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH;
        // First record (folder)
        HFSUnicodeString name = new HFSUnicodeString(params.getVolumeName());
        CatalogKey ck = new CatalogKey(CatalogNodeId.HFSPLUS_POR_CNID, name);
        CatalogFolder folder = new CatalogFolder();
        folder.setFolderId(CatalogNodeId.HFSPLUS_ROOT_CNID);
        folder.setValence(params.isJournaled() ? 2 : 0);
        LeafRecord record = new LeafRecord(ck, folder.getBytes());
        rootNode.addNodeRecord(0, record, offset);
        // Second record (thread)
        offset = offset + ck.getKeyLength() + CatalogFolder.CATALOG_FOLDER_SIZE;
        CatalogKey tck = new CatalogKey(CatalogNodeId.HFSPLUS_ROOT_CNID, name);
        CatalogThread ct = new CatalogThread(HfsPlusConstants.RECORD_TYPE_FOLDER_THREAD,
            CatalogNodeId.HFSPLUS_ROOT_CNID, new HFSUnicodeString(""));
        record = new LeafRecord(tck, ct.getBytes());
        rootNode.addNodeRecord(1, record, offset);
        buffer = ByteBuffer.allocate(bufferLength + bthr.getNodeSize());
        buffer.put(btnd.getBytes());
        buffer.put(bthr.getBytes());
        buffer.position(rootNodePosition);
        buffer.put(rootNode.getBytes());
        buffer.rewind();
    }

    /**
     * @param parentID
     * @return
     * @throws IOException
     */
    public final LeafRecord getRecord(final CatalogNodeId parentID)
        throws IOException {
        int currentOffset = 0;
        LeafRecord lr = null;
        int nodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        fs.getApi().read(catalogHeaderNodeOffset + (getBTHeaderRecord().getRootNode() * nodeSize), nodeData);
        nodeData.rewind();
        byte[] data = ByteBufferUtils.toArray(nodeData);
        CatalogNode node = new CatalogNode(data, nodeSize);
        while (node.isIndexNode()) {
            IndexRecord record = (IndexRecord) node.find(parentID);
            currentOffset = catalogHeaderNodeOffset + (record.getIndex() * nodeSize);
            nodeData = ByteBuffer.allocate(nodeSize);
            fs.getApi().read(currentOffset, nodeData);
            nodeData.rewind();
            data = ByteBufferUtils.toArray(nodeData);
            node = new CatalogNode(data, nodeSize);
        }

        if (node.isLeafNode()) {
            lr = (LeafRecord) node.find(parentID);
        }
        return lr;
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the root node of the tree.
     *
     * @param parentID Parent node id
     * @return Array of LeafRecord
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
     * @param parentID   Parent node id
     * @param nodeNumber Index of node where the search begin.
     * @return Array of LeafRecord
     * @throws IOException
     */
    public final LeafRecord[] getRecords(final CatalogNodeId parentID, final int nodeNumber)
        throws IOException {
        try {
            int currentNodeNumber = nodeNumber;
            int nodeSize = getBTHeaderRecord().getNodeSize();
            ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
            fs.getApi().read(catalogHeaderNodeOffset + (currentNodeNumber * nodeSize), nodeData);
            CatalogNode node = new CatalogNode(nodeData.array(), nodeSize);
            if (node.isIndexNode()) {
                IndexRecord[] records = (IndexRecord[]) node.findChilds(parentID);
                List<LeafRecord> lfList = new LinkedList<LeafRecord>();
                for (IndexRecord rec : records) {
                    LeafRecord[] lfr = getRecords(parentID, rec.getIndex());
                    for (LeafRecord lr : lfr) {
                        lfList.add(lr);
                    }
                }
                return lfList.toArray(new LeafRecord[lfList.size()]);
            } else if (node.isLeafNode()) {
                return (LeafRecord[]) node.findAll(parentID);
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * @param parentID
     * @param nodeName
     * @return
     * @throws IOException
     */
    public final LeafRecord getRecord(final CatalogNodeId parentID, final HFSUnicodeString nodeName)
        throws IOException {
        int currentNodeNumber = getBTHeaderRecord().getRootNode();
        int nodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        fs.getApi().read(catalogHeaderNodeOffset + (currentNodeNumber * nodeSize), buffer);
        CatalogNode node = new CatalogNode(nodeData.array(), nodeSize);
        int currentOffset = 0;
        CatalogKey cKey = new CatalogKey(parentID, nodeName);
        while (node.isIndexNode()) {
            IndexRecord record = (IndexRecord) node.find(cKey);
            currentNodeNumber = record.getIndex();
            currentOffset = catalogHeaderNodeOffset + record.getIndex() * nodeSize;
            nodeData = ByteBuffer.allocate(nodeSize);
            fs.getApi().read(currentOffset, buffer);
            node = new CatalogNode(nodeData.array(), nodeSize);
        }
        LeafRecord lr = null;
        if (node.isLeafNode()) {
            lr = (LeafRecord) node.find(parentID);
        }
        return lr;
    }

    public final NodeDescriptor getBTNodeDescriptor() {
        return btnd;
    }

    public final BTHeaderRecord getBTHeaderRecord() {
        return bthr;
    }

    public ByteBuffer getBytes() {
        return buffer;
    }

}
