/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.fs.hfsplus.tree.NodeRecord;
import org.jnode.util.BigEndian;

public class CatalogNode extends AbstractNode {
    
    /**
     * Create a new node.
     * @param descriptor
     * @param nodeSize
     */
    public CatalogNode(NodeDescriptor descriptor, final int nodeSize) {
        this.descriptor = descriptor;
        this.size = nodeSize;
        this.records = new ArrayList<NodeRecord>(this.descriptor.getNumRecords());
        this.offsets = new ArrayList<Integer>(this.descriptor.getNumRecords() + 1);
        this.offsets.add(Integer.valueOf(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH));
    }

    /**
     * Create node from existing data.
     * @param nodeData
     * @param nodeSize
     */
    public CatalogNode(final byte[] nodeData, final int nodeSize) {
        this.descriptor = new NodeDescriptor(nodeData, 0);
        this.size = nodeSize;
        this.records = new ArrayList<NodeRecord>(this.descriptor.getNumRecords());
        this.offsets = new ArrayList<Integer>(this.descriptor.getNumRecords() + 1);
        for(int i = 0; i < this.descriptor.getNumRecords(); i++){
            offsets.add(BigEndian.getInt16(nodeData, size - ((i + 1) * 2)));
            //TODO Get record data.
        }
        
    }

    @Override
    public NodeRecord getNodeRecord(int index) {
        return records.get(index);
    }

    /**
     * @param parentId
     * @return a NodeRecord or {@code null}
     */
    public final NodeRecord find(final CatalogNodeId parentId) {
        for (NodeRecord record : records) {
            Key key = record.getKey();
            if (key instanceof CatalogKey) {
                if (((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * @param key
     * @return a NodeRecord or {@code null}
     */
    public NodeRecord find(final CatalogKey key) {
        NodeRecord largestMatchingRecord = null;
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            NodeRecord record = this.getNodeRecord(index);
            if ((record.getKey().compareTo(key) <= 0)
                && (record.getKey().compareTo(largestMatchingRecord.getKey()) > 0)) {
                largestMatchingRecord = record;
            }
        }
        return largestMatchingRecord;
    }

    /**
     * @param parentId
     * @return an array of NodeRecords
     */
    public final NodeRecord[] findChildren(final CatalogNodeId parentId) {
        LinkedList<NodeRecord> result = new LinkedList<NodeRecord>();
        NodeRecord largestMatchingRecord = null;
        CatalogKey largestMatchingKey = null;
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            NodeRecord record = this.getNodeRecord(index);
            CatalogKey key = (CatalogKey) record.getKey();
            if (key.getParentId().getId() < parentId.getId()
                && (largestMatchingKey == null || key.compareTo(largestMatchingKey) > 0)) {
                largestMatchingKey = key;
                largestMatchingRecord = record;
            } else if (key.getParentId().getId() == parentId.getId()) {
                result.addLast(record);
            }
        }

        if (largestMatchingKey != null) {
            result.addFirst(largestMatchingRecord);
        }
        return result.toArray(new NodeRecord[result.size()]);
    }

    /**
     * @param parentId
     * @return an array of NodeRecords
     */
    public final NodeRecord[] findAll(final CatalogNodeId parentId) {
        List<NodeRecord> list = new LinkedList<NodeRecord>();
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            NodeRecord record = this.getNodeRecord(index);
            Key key = record.getKey();
            if (key instanceof CatalogKey && ((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                list.add(record);
            }
        }
        return list.toArray(new NodeRecord[list.size()]);
    }

}
