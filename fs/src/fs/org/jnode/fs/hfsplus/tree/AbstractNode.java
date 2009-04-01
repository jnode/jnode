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
 
package org.jnode.fs.hfsplus.tree;

import java.util.List;

import org.jnode.util.BigEndian;

public abstract class AbstractNode implements Node {
    protected NodeDescriptor descriptor;
    protected List<NodeRecord> records;
    protected List<Integer> offsets;
    protected int size;

    @Override
    public NodeDescriptor getNodeDescriptor() {
        return descriptor;
    }

    public boolean isIndexNode() {
        return this.getNodeDescriptor().getKind() == NodeDescriptor.BT_INDEX_NODE;
    }

    public boolean isLeafNode() {
        return this.getNodeDescriptor().getKind() == NodeDescriptor.BT_LEAF_NODE;
    }

    @Override
    public int getRecordOffset(int index) {
        return offsets.get(index);
    }

    @Override
    public abstract NodeRecord getNodeRecord(int index);

    @Override
    public void addNodeRecord(NodeRecord record) {
        Integer lastOffset = offsets.get(offsets.size() - 1);
        Integer newOffset = lastOffset + record.getSize();
        offsets.add(newOffset);
        records.add(record);
    }

    public byte[] getBytes() {
        byte[] datas = new byte[size];
        System.arraycopy(descriptor.getBytes(), 0, datas, 0, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
        int offsetIndex = 0;
        int offset;
        for (NodeRecord record : records) {
            offset = offsets.get(offsetIndex);
            System.arraycopy(record.getBytes(), 0, datas, offset, record.getSize());
            BigEndian.setInt16(datas, size - ((offsetIndex + 1) * 2), offset);
            offsetIndex++;
        }
        offset = offsets.get(offsets.size() - 1);
        BigEndian.setInt16(datas, size - ((offsetIndex + 1) * 2), offset);
        return datas;
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append((this.isLeafNode()) ? "Leaf node" : "Index node").append("\n");
        b.append(this.getNodeDescriptor().toString()).append("\n");
        b.append("Offsets : ").append(offsets.toString());
        return b.toString();
        
    }
}
