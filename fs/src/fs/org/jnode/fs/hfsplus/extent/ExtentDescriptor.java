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
 
package org.jnode.fs.hfsplus.extent;

import org.jnode.util.BigEndian;

public class ExtentDescriptor {

    public static final int EXTENT_DESCRIPTOR_LENGTH = 8;

    private byte[] data;

    /**
     * Create empty extent descriptor.
     */
    public ExtentDescriptor() {
        data = new byte[EXTENT_DESCRIPTOR_LENGTH];
    }

    public ExtentDescriptor(final byte[] src, final int offset) {
        data = new byte[EXTENT_DESCRIPTOR_LENGTH];
        System.arraycopy(src, offset, data, 0, EXTENT_DESCRIPTOR_LENGTH);
    }

    public final int getStartBlock() {
        return BigEndian.getInt32(data, 0);
    }

    public final void setStartBlock(int start) {
        BigEndian.setInt32(data, 0, start);
    }

    public final int getBlockCount() {
        return BigEndian.getInt32(data, 4);
    }

    public final void setBlockCount(int count) {
        BigEndian.setInt32(data, 4, count);
    }

    public final byte[] getBytes() {
        return data;
    }

    public final String toString() {
        return "Start block : " + getStartBlock() + "\tBlock count : " + getBlockCount() + "\n";
    }
}
