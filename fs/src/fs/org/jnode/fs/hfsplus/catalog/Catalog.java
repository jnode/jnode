/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.hfsplus.catalog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.jnode.fs.hfsplus.SuperBlock;
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

    private HfsPlusForkData catalogFile;

    private ByteBuffer buffer;

    /**
     * Create Catalog based on meta-data that exist on the file system.
     * 
     * @param fs HFS+ file system that contains catalog informations.
     * @throws IOException
     */
    public Catalog(final HfsPlusFileSystem fs) throws IOException {
        log.info("Load B-Tree catalog file.");
        this.fs = fs;
        SuperBlock sb = fs.getVolumeHeader();
        catalogFile = sb.getCatalogFile();
        
        if(!catalogFile.getExtent(0).isEmpty()) {
            buffer = ByteBuffer.allocate(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH +
                            BTHeaderRecord.BT_HEADER_RECORD_LENGTH);
            catalogFile.read(fs, 0, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            log.info("Load catalog node descriptor.");
            btnd = new NodeDescriptor(data, 0);
            log.debug(btnd.toString());
            log.info("Load catalog header record.");
            bthr = new BTHeaderRecord(data, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
            log.debug(bthr.toString());

        }
    }

    /**
     * Create new Catalog
     * 
     * @param params
     */
    public Catalog(HFSPlusParams params, HfsPlusFileSystem fs) {
        log.info("Create B-Tree catalog file.");
        this.fs = fs;
        int nodeSize = params.getCatalogNodeSize();
        int bufferLength = 0;
        log.info("Create catalog node descriptor.");
        btnd = new NodeDescriptor(0, 0, NodeDescriptor.BT_HEADER_NODE, 0, 3);
        log.debug(btnd.toString());
        bufferLength += NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH;
        //
        int totalNodes = params.getCatalogClumpSize() / params.getCatalogNodeSize();
        int freeNodes = totalNodes - 2;
        log.info("Create catalog header record.");
        bthr =
                new BTHeaderRecord(1, 1, params.getInitializeNumRecords(), 1, 1, nodeSize,
                        CatalogKey.MAXIMUM_KEY_LENGTH, totalNodes, freeNodes,
                        params.getCatalogClumpSize(), BTHeaderRecord.BT_TYPE_HFS,
                        BTHeaderRecord.KEY_COMPARE_TYPE_CASE_FOLDING,
                        BTHeaderRecord.BT_VARIABLE_INDEX_KEYS_MASK +
                                BTHeaderRecord.BT_BIG_KEYS_MASK);
        log.debug(bthr.toString());
        bufferLength += BTHeaderRecord.BT_HEADER_RECORD_LENGTH;
        log.info("Create root node.");
        int rootNodePosition = bthr.getRootNode() * nodeSize;
        bufferLength += (rootNodePosition - bufferLength);
        // Create node descriptor
        NodeDescriptor nd =
                new NodeDescriptor(0, 0, NodeDescriptor.BT_LEAF_NODE, 1,
                        params.getInitializeNumRecords());
        CatalogLeafNode rootNode = new CatalogLeafNode(nd, nodeSize);
        // First record (folder)
        HfsUnicodeString name = new HfsUnicodeString(params.getVolumeName());
        CatalogKey ck = new CatalogKey(CatalogNodeId.HFSPLUS_POR_CNID, name);
        CatalogFolder folder =
                new CatalogFolder(params.isJournaled() ? 2 : 0, CatalogNodeId.HFSPLUS_ROOT_CNID);
        LeafRecord record = new LeafRecord(ck, folder.getBytes());
        rootNode.addNodeRecord(record);
        // Second record (thread)
        CatalogKey tck = new CatalogKey(CatalogNodeId.HFSPLUS_ROOT_CNID, name);
        CatalogThread ct =
                new CatalogThread(CatalogFolder.RECORD_TYPE_FOLDER_THREAD,
                        CatalogNodeId.HFSPLUS_ROOT_CNID, new HfsUnicodeString(""));
        record = new LeafRecord(tck, ct.getBytes());
        rootNode.addNodeRecord(record);
        log.debug(rootNode.toString());
        buffer = ByteBuffer.allocate(bufferLength + bthr.getNodeSize());
        buffer.put(btnd.getBytes());
        buffer.put(bthr.getBytes());
        buffer.position(rootNodePosition);
        buffer.put(rootNode.getBytes());
        buffer.rewind();
    }

    /**
     * Save catalog file to disk.
     * 
     * @throws IOException
     */
    public void update() throws IOException {
        SuperBlock vh = fs.getVolumeHeader();
        long offset = vh.getCatalogFile().getExtent(0).getStartOffset(vh.getBlockSize());
        fs.getApi().write(offset, this.getBytes());
    }

    /**
     * Create a new node in the catalog B-Tree.
     * 
     * @param filename
     * @param parentId
     * @param nodeId
     * @param nodeType
     * @return the new node instance
     */
    public LeafRecord createNode(String filename, CatalogNodeId parentId, CatalogNodeId nodeId,
            int nodeType) throws IOException {
        HfsUnicodeString name = new HfsUnicodeString(filename);
        LeafRecord record = this.getRecord(parentId, name);
        if (record == null) {
            NodeDescriptor nd = new NodeDescriptor(0, 0, NodeDescriptor.BT_LEAF_NODE, 1, 2);
            CatalogLeafNode node = new CatalogLeafNode(nd, 8192);
            CatalogKey key = new CatalogKey(parentId, name);
            CatalogThread thread = new CatalogThread(nodeType, parentId, name);
            record = new LeafRecord(key, thread.getBytes());
            node.addNodeRecord(record);
            if (nodeType == CatalogFolder.RECORD_TYPE_FOLDER) {
                CatalogFolder folder = new CatalogFolder(0, nodeId);
                key = new CatalogKey(parentId, name);
                record = new LeafRecord(key, folder.getBytes());
            } else {
                // TODO
            }
        } else {
            // TODO
        }
        return record;
    }

    /**
     * @param parentID
     * @return the leaf record, or possibly {code null}.
     * @throws IOException
     */
    public final LeafRecord getRecord(final CatalogNodeId parentID) throws IOException {
        int currentOffset = 0;
        LeafRecord lr = null;
        int nodeSize = bthr.getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        catalogFile.read(fs, (bthr.getRootNode()*nodeSize), nodeData);
        nodeData.rewind();
        byte[] data = ByteBufferUtils.toArray(nodeData);
        NodeDescriptor nd = new NodeDescriptor(nodeData.array(), 0);

        while (nd.isIndexNode()) {
            CatalogIndexNode node = new CatalogIndexNode(data, nodeSize);
            IndexRecord record = (IndexRecord) node.find(parentID);
            currentOffset = record.getIndex() * nodeSize;
            nodeData = ByteBuffer.allocate(nodeSize);
            catalogFile.read(fs, currentOffset, nodeData);
            nodeData.rewind();
            data = ByteBufferUtils.toArray(nodeData);
            nd = new NodeDescriptor(nodeData.array(), 0);
        }

        if (nd.isLeafNode()) {
            CatalogLeafNode node = new CatalogLeafNode(data, nodeSize);
            lr = (LeafRecord) node.find(parentID);
        }
        return lr;
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the root
     * node of the tree.
     * 
     * @param parentID Parent node id
     * @return Array of LeafRecord
     * @throws IOException
     */
    public final LeafRecord[] getRecords(final CatalogNodeId parentID) throws IOException {
        return getRecords(parentID, getBTHeaderRecord().getRootNode());
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the node
     * corresponding to the index passed as parameter.
     * 
     * @param parentID Parent node id
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
            catalogFile.read(fs, (currentNodeNumber * nodeSize), nodeData);
            byte[] datas = nodeData.array();
            NodeDescriptor nd = new NodeDescriptor(datas, 0);
            if (nd.isIndexNode()) {
                CatalogIndexNode node = new CatalogIndexNode(datas, nodeSize);
                IndexRecord[] records = (IndexRecord[]) node.findAll(parentID);
                List<LeafRecord> lfList = new LinkedList<LeafRecord>();
                for (IndexRecord rec : records) {
                    LeafRecord[] lfr = getRecords(parentID, rec.getIndex());
                    for (LeafRecord lr : lfr) {
                        lfList.add(lr);
                    }
                }
                return lfList.toArray(new LeafRecord[lfList.size()]);
            } else if (nd.isLeafNode()) {
                CatalogLeafNode node = new CatalogLeafNode(nodeData.array(), nodeSize);
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
     * @return the leaf node or {@code null}
     * @throws IOException
     */
    public final LeafRecord getRecord(final CatalogNodeId parentID, final HfsUnicodeString nodeName)
        throws IOException {
        int currentNodeNumber = getBTHeaderRecord().getRootNode();
        int nodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        catalogFile.read(fs, (currentNodeNumber * nodeSize), nodeData);
        NodeDescriptor nd = new NodeDescriptor(nodeData.array(), 0);
        int currentOffset = 0;
        CatalogKey cKey = new CatalogKey(parentID, nodeName);
        while (nd.isIndexNode()) {
            CatalogIndexNode node = new CatalogIndexNode(nodeData.array(), nodeSize);
            IndexRecord record = node.find(cKey);
            currentNodeNumber = record.getIndex();
            currentOffset = record.getIndex() * nodeSize;
            nodeData = ByteBuffer.allocate(nodeSize);
            catalogFile.read(fs, currentOffset, buffer);
            node = new CatalogIndexNode(nodeData.array(), nodeSize);
        }
        LeafRecord lr = null;
        if (nd.isLeafNode()) {
            CatalogLeafNode node = new CatalogLeafNode(nodeData.array(), nodeSize);
            lr = node.find(parentID);
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
