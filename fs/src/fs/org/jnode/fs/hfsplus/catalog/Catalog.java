/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import java.util.Collections;
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
import org.jnode.util.NumberUtils;

public class Catalog {

    private static final Logger log = Logger.getLogger(Catalog.class);
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
        log.debug("Load B-Tree catalog file.");
        this.fs = fs;
        SuperBlock sb = fs.getVolumeHeader();
        catalogFile = sb.getCatalogFile();

        if (!catalogFile.getExtent(0).isEmpty()) {
            buffer = ByteBuffer.allocate(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH +
                BTHeaderRecord.BT_HEADER_RECORD_LENGTH);
            catalogFile.read(fs, 0, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            log.debug("Load catalog node descriptor.");
            btnd = new NodeDescriptor(data, 0);
            log.debug(btnd.toString());
            log.debug("Load catalog header record.");
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
        log.debug("Create B-Tree catalog file.");
        this.fs = fs;
        int nodeSize = params.getCatalogNodeSize();
        int bufferLength = 0;
        log.debug("Create catalog node descriptor.");
        btnd = new NodeDescriptor(0, 0, NodeDescriptor.BT_HEADER_NODE, 0, 3);
        log.debug(btnd.toString());
        bufferLength += NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH;
        //
        int totalNodes = params.getCatalogClumpSize() / params.getCatalogNodeSize();
        int freeNodes = totalNodes - 2;
        log.debug("Create catalog header record.");
        bthr =
            new BTHeaderRecord(1, 1, params.getInitializeNumRecords(), 1, 1, nodeSize,
                CatalogKey.MAXIMUM_KEY_LENGTH, totalNodes, freeNodes,
                params.getCatalogClumpSize(), BTHeaderRecord.BT_TYPE_HFS,
                BTHeaderRecord.KEY_COMPARE_TYPE_CASE_FOLDING,
                BTHeaderRecord.BT_VARIABLE_INDEX_KEYS_MASK +
                    BTHeaderRecord.BT_BIG_KEYS_MASK);
        log.debug(bthr.toString());
        bufferLength += BTHeaderRecord.BT_HEADER_RECORD_LENGTH;
        log.debug("Create root node.");
        long rootNodePosition = bthr.getRootNode() * nodeSize;
        bufferLength += (rootNodePosition - bufferLength);
        CatalogLeafNode rootNode = createRootNode(params);
        buffer = ByteBuffer.allocate(bufferLength + bthr.getNodeSize());
        buffer.put(btnd.getBytes());
        buffer.put(bthr.getBytes());
        buffer.position((int) rootNodePosition);
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

    private CatalogLeafNode createRootNode(HFSPlusParams params) {
        int nodeSize = params.getCatalogNodeSize();
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
        CatalogKey tck = new CatalogKey(CatalogNodeId.HFSPLUS_POR_CNID, name);
        CatalogThread ct =
            new CatalogThread(CatalogFolder.RECORD_TYPE_FOLDER_THREAD,
                CatalogNodeId.HFSPLUS_ROOT_CNID, new HfsUnicodeString(""));
        record = new LeafRecord(tck, ct.getBytes());
        rootNode.addNodeRecord(record);
        log.debug(rootNode.toString());
        return rootNode;
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
    public CatalogLeafNode createNode(String filename, CatalogNodeId parentId, CatalogNodeId nodeId,
                                      int nodeType) throws IOException {
        CatalogLeafNode node;
        HfsUnicodeString name = new HfsUnicodeString(filename);
        // find parent leaf record.
        LeafRecord record = this.getRecord(parentId, name);
        if (record == null) {
            NodeDescriptor nd = new NodeDescriptor(0, 0, NodeDescriptor.BT_LEAF_NODE, 1, 2);
            node = new CatalogLeafNode(nd, 8192);
            // Normal record
            CatalogKey key = new CatalogKey(parentId, name);
            if (nodeType == CatalogFolder.RECORD_TYPE_FOLDER) {
                CatalogFolder folder = new CatalogFolder(0, parentId);
                key = new CatalogKey(parentId, name);
                record = new LeafRecord(key, folder.getBytes());
                node.addNodeRecord(record);
            } else {
                // Catalog file
            }
            // Thread record
            key = new CatalogKey(parentId, name);
            int threadType;
            if (nodeType == CatalogFolder.RECORD_TYPE_FOLDER) {
                threadType = CatalogFolder.RECORD_TYPE_FOLDER_THREAD;
            } else {
                threadType = CatalogFile.RECORD_TYPE_FILE_THREAD;
            }
            CatalogThread thread = new CatalogThread(threadType, nodeId, name);
            record = new LeafRecord(key, thread.getBytes());
            node.addNodeRecord(record);

        } else {
            throw new IOException("Leaf record for parent (" + parentId.getId() + ") doesn't exist.");
        }
        return node;
    }

    /**
     * @param parentID
     * @return the leaf record, or possibly {code null}.
     * @throws IOException
     */
    public final LeafRecord getRecord(final CatalogNodeId parentID) throws IOException {
        long currentOffset = 0;
        LeafRecord lr = null;
        int nodeSize = bthr.getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        catalogFile.read(fs, (bthr.getRootNode() * nodeSize), nodeData);
        nodeData.rewind();
        byte[] data = ByteBufferUtils.toArray(nodeData);
        NodeDescriptor nd = new NodeDescriptor(data, 0);

        while (nd.isIndexNode()) {
            CatalogIndexNode node = new CatalogIndexNode(data, nodeSize);
            IndexRecord record = (IndexRecord) node.find(new CatalogKey(parentID));
            currentOffset = record.getIndex() * nodeSize;
            nodeData = ByteBuffer.allocate(nodeSize);
            catalogFile.read(fs, currentOffset, nodeData);
            nodeData.rewind();
            data = ByteBufferUtils.toArray(nodeData);
            nd = new NodeDescriptor(nodeData.array(), 0);
        }

        if (nd.isLeafNode()) {
            CatalogLeafNode node = new CatalogLeafNode(data, nodeSize);
            lr = (LeafRecord) node.find(new CatalogKey(parentID));
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
    public final List<LeafRecord> getRecords(final CatalogNodeId parentID) throws IOException {
        return getRecords(parentID, getBTHeaderRecord().getRootNode());
    }

    /**
     * Find leaf records corresponding to parentID. The search begin at the node
     * corresponding to the index passed as parameter.
     *
     * @param parentID   Parent node id
     * @param nodeNumber Index of node where the search begin.
     * @return Array of LeafRecord
     * @throws IOException
     */
    public final List<LeafRecord> getRecords(final CatalogNodeId parentID, final long nodeNumber)
        throws IOException {
        try {
            long currentNodeNumber = nodeNumber;
            int nodeSize = getBTHeaderRecord().getNodeSize();
            ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
            catalogFile.read(fs, (currentNodeNumber * nodeSize), nodeData);
            byte[] datas = nodeData.array();
            NodeDescriptor nd = new NodeDescriptor(datas, 0);
            if (nd.isIndexNode()) {
                CatalogIndexNode node = new CatalogIndexNode(datas, nodeSize);
                IndexRecord[] records = node.findAll(new CatalogKey(parentID));
                List<LeafRecord> lfList = new LinkedList<LeafRecord>();
                for (IndexRecord rec : records) {
                    List<LeafRecord> lfr = getRecords(parentID, rec.getIndex());
                    lfList.addAll(lfr);
                }
                return lfList;
            } else if (nd.isLeafNode()) {
                CatalogLeafNode node = new CatalogLeafNode(nodeData.array(), nodeSize);
                return node.findAll(new CatalogKey(parentID));
            } else {
                log.info(String.format("Node %d wasn't a leaf or index: %s\n%s", nodeNumber, nd, NumberUtils.hex(datas)));
                return Collections.emptyList();
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
        long currentNodeNumber = getBTHeaderRecord().getRootNode();
        int nodeSize = getBTHeaderRecord().getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        catalogFile.read(fs, (currentNodeNumber * nodeSize), nodeData);
        NodeDescriptor nd = new NodeDescriptor(nodeData.array(), 0);
        long currentOffset = 0;
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
            lr = node.find(cKey);
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
