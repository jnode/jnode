/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.fs.hfsplus.attributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.fs.hfsplus.SuperBlock;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.BTHeaderRecord;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.util.BigEndian;
import org.jnode.util.ByteBufferUtils;

/**
 * The attributes file in the HFS+ volume.
 *
 * @author Luke Quinane
 */
public class Attributes {
    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(Attributes.class);

    /**
     * B-Tree Header record
     */
    private BTHeaderRecord bthr;

    /**
     * The current file system.
     */
    private HfsPlusFileSystem fs;

    /**
     * The attributes file data fork.
     */
    private HfsPlusForkData attributesFile;

    /**
     * Creates a new attributes file for the give file system.
     *
     * @param fs the file system.
     * @throws IOException if an error occurs.
     */
    public Attributes(HfsPlusFileSystem fs) throws IOException {
        log.debug("Loading the attributes file B-Tree");
        this.fs = fs;
        SuperBlock sb = fs.getVolumeHeader();
        attributesFile = sb.getAttributesFile();

        int readLength = NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH + BTHeaderRecord.BT_HEADER_RECORD_LENGTH;

        if (!attributesFile.getExtent(0).isEmpty() && attributesFile.getTotalSize() > readLength) {
            ByteBuffer buffer = ByteBuffer.allocate(readLength);
            attributesFile.read(fs, 0, buffer);
            buffer.rewind();
            byte[] data = ByteBufferUtils.toArray(buffer);
            log.debug("Load attributes node descriptor.");

            NodeDescriptor btnd = new NodeDescriptor(data, 0);
            log.debug(btnd.toString());
            log.debug("Load attributes header record.");
            bthr = new BTHeaderRecord(data, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
            log.debug(bthr.toString());
        }
    }

    /**
     * Gets all attributes for the given file.
     *
     * @param fileId the ID of the file to look up the attributes.
     * @return the list of attributes.
     * @throws IOException if an error occurs.
     */
    public List<String> getAllAttributes(CatalogNodeId fileId) throws IOException {
        if (bthr == null) {
            return null;
        }

        List<String> attributes = new ArrayList<String>();

        for (LeafRecord record : getAttributeLeafRecords(fileId, null, bthr.getRootNode())) {
            if (record != null) {
                attributes.add(((AttributeKey) record.getKey()).getAttributeName().getUnicodeString());
            }
        }

        return attributes;
    }

    /**
     * Looks up an attribute in the attributes file.
     *
     * @param fileId        the ID of the file to look up the attribute on.
     * @param attributeName the name of the attribute to lookup.
     * @return the attribute data, or possibly {code null}.
     * @throws IOException if an error occurs.
     */
    public AttributeData getAttribute(CatalogNodeId fileId, String attributeName) throws IOException {
        if (bthr == null) {
            return null;
        }

        List<LeafRecord> records = getAttributeLeafRecords(fileId, attributeName, bthr.getRootNode());
        if (records.isEmpty()) {
            return null;
        }

        if (records.size() > 1) {
            log.warn("Expected a single attribute but got: " + records);
        }

        return toAttributeData(fileId, records.get(0));
    }

    /**
     * Looks up an attribute in the attributes file.
     *
     * @param fileId        the ID of the file to look up the attribute on.
     * @param attributeName the name of the attribute to lookup.
     * @param nodeNumber    the index of node where the search begin.
     * @return the attribute data, or possibly {code null}.
     * @throws IOException if an error occurs.
     */
    public List<LeafRecord> getAttributeLeafRecords(CatalogNodeId fileId, String attributeName, long nodeNumber)
        throws IOException {
        if (attributesFile.getExtent(0).isEmpty()) {
            // No attributes
            return Collections.emptyList();
        }

        int nodeSize = bthr.getNodeSize();
        ByteBuffer nodeData = ByteBuffer.allocate(nodeSize);
        attributesFile.read(fs, (nodeNumber * nodeSize), nodeData);
        nodeData.rewind();
        byte[] data = ByteBufferUtils.toArray(nodeData);
        NodeDescriptor nodeDescriptor = new NodeDescriptor(data, 0);

        if (nodeDescriptor.isIndexNode()) {
            AttributeIndexNode node = new AttributeIndexNode(data, nodeSize);
            IndexRecord[] records = node.findAll(new AttributeKey(fileId, attributeName));

            List<LeafRecord> leafRecords = new LinkedList<LeafRecord>();
            for (IndexRecord indexRecord : records) {
                leafRecords.addAll(getAttributeLeafRecords(fileId, attributeName, indexRecord.getIndex()));
            }
            return leafRecords;

        } else if (nodeDescriptor.isLeafNode()) {
            AttributeLeafNode node = new AttributeLeafNode(data, nodeSize);
            return node.findAll(new AttributeKey(fileId, attributeName));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Converts a leaf record into an attribute data.
     *
     * @param fileId     the file ID.
     * @param leafRecord the leaf record.
     * @return the attribute data, or {@code null} if the record cannot be converted.
     */
    public AttributeData toAttributeData(CatalogNodeId fileId, LeafRecord leafRecord) {

        long type = BigEndian.getUInt32(leafRecord.getData(), 0);

        if (type == AttributeData.ATTRIBUTE_INLINE_DATA) {
            return new AttributeInlineData(leafRecord.getData(), 0);
        } else if (type == AttributeData.ATTRIBUTE_FORK_DATA) {
            return new AttributeForkData(fileId, leafRecord.getData(), 0);
        } else if (type == AttributeData.ATTRIBUTE_EXTENTS) {
            throw new UnsupportedOperationException();
        } else {
            log.warn(String.format("Invalid attribute record type: %d for leaf: %s", type, leafRecord));
            return null;
        }
    }
}
