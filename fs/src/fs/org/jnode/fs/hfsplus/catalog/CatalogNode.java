/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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

import java.util.LinkedList;
import java.util.List;

import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.fs.hfsplus.tree.NodeRecord;

public class CatalogNode extends AbstractNode {

    public CatalogNode(NodeDescriptor descriptor, final int nodeSize) {
        this.size = nodeSize;
        this.datas = new byte[nodeSize];
        System.arraycopy(descriptor.getBytes(), 0, datas, 0, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
    }

    public CatalogNode(final byte[] nodeData, final int nodeSize) {
        this.size = nodeSize;
        this.datas = nodeData;
    }

    @Override
    public NodeRecord getNodeRecord(int index) {
        int offset = this.getRecordOffset(index);
        int offset2 = this.getRecordOffset(index + 1);
        int recordSize = offset2 - offset;
        NodeRecord record = null;
        Key key = new CatalogKey(datas, offset);
        if (isIndexNode()) {
            record = new IndexRecord(key, datas, offset);
        } else {
            record = new LeafRecord(key, datas, offset, recordSize);
        }
        return record;
    }

    /**
     * @param parentId
     * @return
     */
    public final NodeRecord find(final CatalogNodeId parentId) {
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            NodeRecord record = this.getNodeRecord(index);
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
     * @return
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
     * @return
     */
    public final NodeRecord[] findChilds(final CatalogNodeId parentId) {
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
     * @return
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
