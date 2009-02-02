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

import org.jnode.util.BigEndian;

public class BTHeaderRecord {
    public static final int BT_HEADER_RECORD_LENGTH = 106;
    private byte[] data;

    public BTHeaderRecord() {
        data = new byte[BT_HEADER_RECORD_LENGTH];
    }

    public BTHeaderRecord(final byte[] src, int offset) {
        data = new byte[BT_HEADER_RECORD_LENGTH];
        System.arraycopy(src, offset, data, 0, BT_HEADER_RECORD_LENGTH);
    }

    public final int getTreeDepth() {
        return BigEndian.getInt16(data, 0);
    }

    public void setTreeDepth(int depth) {
        BigEndian.setInt16(data, 0, depth);
    }

    public final int getRootNode() {
        return BigEndian.getInt32(data, 2);
    }

    public void setRootNode(int node) {
        BigEndian.setInt32(data, 2, node);
    }

    public final int getLeafRecords() {
        return BigEndian.getInt32(data, 6);
    }

    public void setLeafRecords(int count) {
        BigEndian.setInt32(data, 6, count);
    }

    public final int getFirstLeafNode() {
        return BigEndian.getInt32(data, 10);
    }

    public void settFirstLeafNode(int node) {
        BigEndian.setInt32(data, 10, node);
    }

    public final int getLastLeafNode() {
        return BigEndian.getInt32(data, 14);
    }

    public void setLastLeafNode(int node) {
        BigEndian.setInt32(data, 14, node);
    }

    public final int getNodeSize() {
        return BigEndian.getInt16(data, 18);
    }

    public void setNodeSize(int size) {
        BigEndian.setInt16(data, 18, size);
    }

    public int getMaxKeyLength() {
        return BigEndian.getInt16(data, 20);
    }

    public void setMaxKeyLength(int length) {
        BigEndian.setInt16(data, 20, length);
    }

    public int getTotalNodes() {
        return BigEndian.getInt32(data, 22);
    }

    public void setTotalNodes(int count) {
        BigEndian.setInt32(data, 22, count);
    }

    public int getFreeNodes() {
        return BigEndian.getInt32(data, 26);
    }

    public void setFreeNodes(int count) {
        BigEndian.setInt32(data, 26, count);
    }

    public int getClumpSize() {
        return BigEndian.getInt32(data, 32);
    }

    public void setClumpSize(int size) {
        BigEndian.setInt32(data, 32, size);
    }

    public int getTreeType() {
        return BigEndian.getInt8(data, 36);
    }

    public void setTreeType(int type) {
        BigEndian.setInt8(data, 36, type);
    }

    public int getKeyCompareType() {
        return BigEndian.getInt8(data, 37);
    }

    public void setKeyCompareType(int type) {
        BigEndian.setInt8(data, 38, type);
    }

    public long getAttributes() {
        return BigEndian.getInt32(data, 39);
    }

    public void setAttributes(int attrs) {
        BigEndian.setInt32(data, 39, attrs);
    }

    public byte[] getBytes() {
        return data;
    }
    
    public final String toString() {
        return ("Root node:  " + getRootNode() + "\n" + "First leaf: "
                + getFirstLeafNode() + "\n" + "Last leaf:  "
                + getLastLeafNode() + "\n" + "node size:  " + getNodeSize() + "\n");
    }
}
