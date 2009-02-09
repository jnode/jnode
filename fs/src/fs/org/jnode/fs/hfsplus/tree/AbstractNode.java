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

import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.util.BigEndian;

public abstract class AbstractNode implements Node {

    protected byte[] datas;
    protected int size;

    @Override
    public NodeDescriptor getNodeDescriptor() {
        return new NodeDescriptor(datas, 0);
    }

    public boolean isIndexNode() {
        return this.getNodeDescriptor().getKind() == HfsPlusConstants.BT_INDEX_NODE;
    }

    public boolean isLeafNode() {
        return this.getNodeDescriptor().getKind() == HfsPlusConstants.BT_LEAF_NODE;
    }

    @Override
    public int getRecordOffset(int index) {
        return BigEndian.getInt16(datas, size - ((index + 1) * 2));
    }

    @Override
    public abstract NodeRecord getNodeRecord(int index);

    @Override
    public void addNodeRecord(int index, NodeRecord record, int offset) {
        BigEndian.setInt16(datas, size - ((index + 1) * 2), offset);
        System.arraycopy(record.getBytes(), 0, datas, offset, record.getSize());
    }

    public byte[] getBytes() {
        return datas;
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append((this.isLeafNode()) ? "Leaf node" : "Index node").append("\n");
        b.append(this.getNodeDescriptor().toString());
        return b.toString();
        
    }
}
