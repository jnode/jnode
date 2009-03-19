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

public class NodeDescriptor {
    public static final int BT_LEAF_NODE = -1;
    public static final int BT_INDEX_NODE = 0;
    public static final int BT_HEADER_NODE = 1;
    public static final int BT_MAP_NODE = 2;
    /** The size of the node descriptor. */
    public static final int BT_NODE_DESCRIPTOR_LENGTH = 14;
    /** The number of the next node. */
    private int fLink;
    /** The number of the previous node. */
    private int bLink;
    /** The type of the node. */
    private int kind;
    /** The depth of this node in the B-Tree. */
    private int height;
    /** The number of records in this node. */
    private int numRecords;

    /**
     * Creates a new node descriptor.
     * 
     * @param fLink
     * @param bLink
     * @param kind
     * @param height
     * @param numRecords
     */
    public NodeDescriptor(int fLink, int bLink, int kind, int height, int numRecords) {
        this.fLink = fLink;
        this.bLink = bLink;
        this.kind = kind;
        this.height = height;
        this.numRecords = numRecords;
    }

    /**
     * Creates node descriptor from existing data.
     * 
     * @param src byte array contains node descriptor data.
     * @param offset start of node descriptor data.
     */
    public NodeDescriptor(final byte[] src, int offset) {
        byte[] data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
        System.arraycopy(src, offset, data, 0, BT_NODE_DESCRIPTOR_LENGTH);
        fLink = BigEndian.getInt32(data, 0);
        bLink = BigEndian.getInt32(data, 4);
        kind = BigEndian.getInt8(data, 8);
        height = BigEndian.getInt8(data, 9);
        numRecords = BigEndian.getInt16(data, 10);
    }

    /**
     * 
     * @return
     */
    public byte[] getBytes() {
        byte[] data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
        BigEndian.setInt32(data, 0, fLink);
        BigEndian.setInt32(data, 4, bLink);
        BigEndian.setInt8(data, 8, kind);
        BigEndian.setInt8(data, 9, height);
        BigEndian.setInt16(data, 10, numRecords);
        return data;
    }

    public final String toString() {
        return ("FLink:  " + getFLink() + "\n" + "BLink:  " + getBLink() + "\n" + "Kind:   " +
                getKind() + "\n" + "height: " + getHeight() + "\n" + "#rec:   " + getNumRecords() + "\n");
    }

    public int getFLink() {
        return fLink;
    }

    public int getBLink() {
        return bLink;
    }

    public int getKind() {
        return kind;
    }

    public int getHeight() {
        return height;
    }

    public int getNumRecords() {
        return numRecords;
    }

}
