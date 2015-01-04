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
 
package org.jnode.fs.hfsplus.tree;

import java.util.LinkedList;
import org.apache.log4j.Logger;

public abstract class AbstractIndexNode<K extends Key> extends AbstractNode<K, IndexRecord> {

    private static final Logger log = Logger.getLogger(AbstractIndexNode.class);

    /**
     * Create a new node.
     *
     * @param descriptor
     * @param nodeSize
     */
    public AbstractIndexNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData
     * @param nodeSize
     */
    public AbstractIndexNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);
    }

    @Override
    protected IndexRecord createRecord(Key key, byte[] nodeData, int offset, int recordSize) {
        return new IndexRecord(key, nodeData, offset);
    }

    /**
     * Finds all records with the given parent key.
     *
     * @param key the parent key.
     * @return an array of NodeRecords
     */
    public final IndexRecord[] findAll(final K key) {
        LinkedList<IndexRecord> result = new LinkedList<IndexRecord>();
        IndexRecord largestMatchingRecord = null;
        K largestMatchingKey = null;

        for (IndexRecord record : records) {
            K recordKey = (K) record.getKey();

            if (recordKey.compareTo(key) < 0) {
                // The keys/records should be sorted in this index record so take the highest key less than the parent
                largestMatchingKey = recordKey;
                largestMatchingRecord = record;
            } else if (recordKey.equals(key)) {
                result.addLast(record);
            }
        }

        if (largestMatchingKey != null) {
            result.addFirst(largestMatchingRecord);
        }

        return result.toArray(new IndexRecord[result.size()]);
    }
}

