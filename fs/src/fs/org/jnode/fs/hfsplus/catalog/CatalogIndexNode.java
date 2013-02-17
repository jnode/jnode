/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogIndexNode extends AbstractNode<IndexRecord> {

    /**
     * Create a new node.
     * 
     * @param descriptor
     * @param nodeSize
     */
    public CatalogIndexNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     * 
     * @param nodeData
     * @param nodeSize
     */
    public CatalogIndexNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);

    }

    @Override
    protected void loadRecords(final byte[] nodeData) {
        CatalogKey key;
        int offset;
        for (int i = 0; i < this.descriptor.getNumRecords(); i++) {
            offset = offsets.get(i);
            key = new CatalogKey(nodeData, offset);
            records.add(new IndexRecord(key, nodeData, offset));
        }
    }

    /**
     * @param parentId
     * @return a NodeRecord or {@code null}
     */
    public final IndexRecord find(final CatalogNodeId parentId) {
        for (IndexRecord record : records) {
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
     * Find node record based on it's key.
     * 
     * @param key The key to search.
     * @return a NodeRecord or {@code null}
     */
    public IndexRecord find(final CatalogKey key) {
        IndexRecord largestMatchingRecord = null;
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            IndexRecord record = this.getNodeRecord(index);
            if ((record.getKey().compareTo(key) <= 0)) {
                if (largestMatchingRecord != null &&
                        record.getKey().compareTo(largestMatchingRecord.getKey()) > 0) {
                    largestMatchingRecord = record;
                }
            }
        }
        return largestMatchingRecord;
    }

    /**
     * @param parentId
     * @return an array of NodeRecords
     */
    public final IndexRecord[] findAll(final CatalogNodeId parentId) {
        LinkedList<IndexRecord> result = new LinkedList<IndexRecord>();
        IndexRecord largestMatchingRecord = null;
        CatalogKey largestMatchingKey = null;
        for (IndexRecord record : records) {
            CatalogKey key = (CatalogKey) record.getKey();
            if (key.getParentId().getId() < parentId.getId() &&
                    (largestMatchingKey == null || key.compareTo(largestMatchingKey) > 0)) {
                largestMatchingKey = key;
                largestMatchingRecord = record;
            } else if (key.getParentId().getId() == parentId.getId()) {
                result.addLast(record);
            }
        }

        if (largestMatchingKey != null) {
            result.addFirst(largestMatchingRecord);
        }
        return result.toArray(new IndexRecord[result.size()]);
    }

}
