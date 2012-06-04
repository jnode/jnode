/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.util.ArrayList;
import java.util.List;

import org.jnode.util.BigEndian;

public abstract class AbstractNode<T extends NodeRecord> implements Node<T> {
    protected NodeDescriptor descriptor;
    protected List<T> records;
    protected List<Integer> offsets;
    protected int size;

    public AbstractNode(NodeDescriptor descriptor, final int nodeSize) {
        this.descriptor = descriptor;
        this.size = nodeSize;
        this.records = new ArrayList<T>(descriptor.getNumRecords());
        this.offsets = new ArrayList<Integer>(descriptor.getNumRecords() + 1);
        this.offsets.add(Integer.valueOf(NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH));
    }

    public AbstractNode(final byte[] nodeData, final int nodeSize) {
        this.descriptor = new NodeDescriptor(nodeData, 0);
        this.size = nodeSize;
        this.records = new ArrayList<T>(this.descriptor.getNumRecords());
        this.offsets = new ArrayList<Integer>(this.descriptor.getNumRecords() + 1);
        int offset;
        for (int i = 0; i < this.descriptor.getNumRecords() + 1; i++) {
            offset = BigEndian.getInt16(nodeData, size - ((i + 1) * 2));
            offsets.add(Integer.valueOf(offset));
        }
        loadRecords(nodeData);
    }

    protected abstract void loadRecords(final byte[] nodeData);

    @Override
    public NodeDescriptor getNodeDescriptor() {
        return descriptor;
    }

    @Override
    public int getRecordOffset(int index) {
        return offsets.get(index);
    }

    @Override
    public T getNodeRecord(int index) {
        return records.get(index);
    }

    @Override
    public boolean addNodeRecord(T record) {
        int freeSpace = getFreeSize();
        if (freeSpace < record.getSize() + 2) {
            return false;
        }
        Integer lastOffset = offsets.get(offsets.size() - 1);
        Integer newOffset = lastOffset + record.getSize();
        offsets.add(newOffset);
        records.add(record);
        return true;
    }

    public boolean check(int treeHeigth) {
        // Node type is correct.
        if (this.getNodeDescriptor().getKind() < NodeDescriptor.BT_LEAF_NODE ||
                this.getNodeDescriptor().getKind() > NodeDescriptor.BT_MAP_NODE) {
            return false;
        }

        if (this.getNodeDescriptor().getHeight() > treeHeigth) {
            return false;
        }
        return true;
    }

    /**
     * Return amount of free space remaining.
     * 
     * @return remaining free space.
     */
    protected int getFreeSize() {
        int freeOffset = offsets.get(offsets.size() - 1);
        int freeSize = size - freeOffset - (descriptor.getNumRecords() << 1) - OFFSET_SIZE;
        return freeSize;
    }

    public byte[] getBytes() {
        byte[] datas = new byte[size];
        System.arraycopy(descriptor.getBytes(), 0, datas, 0,
                NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
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
        b.append((this.getNodeDescriptor().isLeafNode()) ? "Leaf node" : "Index node").append("\n");
        b.append(this.getNodeDescriptor().toString()).append("\n");
        b.append("Offsets : ").append(offsets.toString());
        return b.toString();

    }
}
