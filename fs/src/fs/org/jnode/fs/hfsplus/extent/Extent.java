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
 
package org.jnode.fs.hfsplus.extent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.fs.hfsplus.SuperBlock;
import org.jnode.fs.hfsplus.tree.BTHeaderRecord;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.util.ByteBufferUtils;
import org.jnode.util.NumberUtils;

public class Extent {
    private static final Logger log = Logger.getLogger(Extent.class);

    /**
     * B-Tree node descriptor
     */
    private NodeDescriptor btnd;

    /**
     * B-Tree Header record
     */
    private BTHeaderRecord bthr;

    /**
     * The current file system.
     */
    private HfsPlusFileSystem fs;

    /**
     * The extent file data fork.
     */
    private HfsPlusForkData extentFile;

    private ByteBuffer buffer;

    public Extent(HFSPlusParams params) {
        log.debug("Create B-Tree extent file.");
        btnd = new NodeDescriptor(0, 0, NodeDescriptor.BT_HEADER_NODE, 0, 3);
        //
        int totalNodes = params.getExtentClumpSize() / params.getExtentNodeSize();
        int freeNodes = totalNodes - 1;
        bthr = new BTHeaderRecord(0, 0, 0, 0, 0, params.getExtentNodeSize(),
            ExtentKey.MAXIMUM_KEY_LENGTH, totalNodes, freeNodes,
            params.getExtentClumpSize(), BTHeaderRecord.BT_TYPE_HFS,
            BTHeaderRecord.KEY_COMPARE_TYPE_CASE_FOLDING,
            BTHeaderRecord.BT_BIG_KEYS_MASK);
    }

    public Extent(HfsPlusFileSystem fs) throws IOException {
        log.debug("Load B-Tree extent overflow file.");
        this.fs = fs;
        SuperBlock sb = fs.getVolumeHeader();
        extentFile = sb.getExtentsFile();

        if (!extentFile.getExtent(0).isEmpty()) {
            buffer = ByteBuffer.allocate(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH +
                BTHeaderRecord.BT_HEADER_RECORD_LENGTH);
            extentFile.read(fs, 0, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            log.debug("Load extent node descriptor.");
            btnd = new NodeDescriptor(data, 0);
            log.debug(btnd.toString());
            log.debug("Load extent header record.");
            bthr = new BTHeaderRecord(data, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
            log.debug(bthr.toString());
        }
    }

    /**
     * Gets all overflow extents that match the given key.
     *
     * @param key the key to match.
     * @return the overflow extents.
     * @throws IOException if an error occurs.
     */
    public final List<ExtentDescriptor> getOverflowExtents(final ExtentKey key) throws IOException {
        return getOverflowExtents(key, bthr.getRootNode());
    }

    /**
     * Gets all overflow extents that match the given key.
     *
     * @param key        the key to match.
     * @param nodeNumber the current node to read from.
     * @return the overflow extents.
     * @throws IOException if an error occurs.
     */
    public final List<ExtentDescriptor> getOverflowExtents(final ExtentKey key, long nodeNumber) throws IOException {
        try {
            long currentNodeNumber = nodeNumber;
            int nodeSize = bthr.getNodeSize();
            ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
            extentFile.read(fs, (currentNodeNumber * nodeSize), nodeData);
            byte[] data = nodeData.array();
            NodeDescriptor nd = new NodeDescriptor(data, 0);

            if (nd.isIndexNode()) {
                ExtentNode extentNode = new ExtentNode(data, nodeSize);

                IndexRecord[] records = extentNode.findAll(key);
                List<ExtentDescriptor> overflowExtents = new LinkedList<ExtentDescriptor>();
                for (IndexRecord record : records) {
                    overflowExtents.addAll(getOverflowExtents(key, record.getIndex()));
                }

                return overflowExtents;

            } else if (nd.isLeafNode()) {
                ExtentLeafNode node = new ExtentLeafNode(nodeData.array(), nodeSize);
                return node.getOverflowExtents(key);

            } else {
                log.info(String.format("Node %d wasn't a leaf or index: %s\n%s", nodeNumber, nd, NumberUtils.hex(data)));
                return Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
