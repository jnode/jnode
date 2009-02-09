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
 
package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class ExtentKey extends AbstractKey {
    
    public static final byte DATA_FORK = (byte) 0x00;
    public static final byte RESOURCE_FORK = (byte) 0xFF;
    public static final int KEY_LENGTH = 12;

    byte[] ek;

    public ExtentKey(final byte[] src, final int offset) {
        ek = new byte[KEY_LENGTH];
        System.arraycopy(src, offset, ek, 0, KEY_LENGTH);
    }

    @Override
    public final int getKeyLength() {
        return BigEndian.getInt16(ek, 0);
    }
   
    public final int getForkType() {
        return BigEndian.getInt8(ek, 2);
    }

    public final int getPad() {
        return BigEndian.getInt8(ek, 3);
    }

    public final CatalogNodeId getCatalogNodeId() {
        return new CatalogNodeId(ek, 4);
    }

    public final int getStartBlock() {
        return BigEndian.getInt32(ek, 8);
    }

    @Override
    public final int compareTo(final Key key) {
        int res = -1;
        if (key instanceof ExtentKey) {
            ExtentKey compareKey = (ExtentKey) key;
            res = getCatalogNodeId().compareTo(compareKey.getCatalogNodeId());
            if (res == 0) {
                res = compareForkType(compareKey.getForkType());
                if (res == 0) {
                    return compareStartBlock(compareKey.getStartBlock());
                }
            }
        }
        return res;
    }

    @Override
    public byte[] getBytes() {
        byte[] data = new byte[this.getKeyLength()];
        return data;
    }
    
    private int compareForkType(int fork) {
        Integer currentForkType = Integer.valueOf(this.getForkType());
        Integer forkType = Integer.valueOf(fork);
        return currentForkType.compareTo(forkType);
    }
    
    private int compareStartBlock(int block) {
        Integer currentStartBlock = Integer.valueOf(this.getStartBlock());
        Integer startBlock = Integer.valueOf(block);
        return currentStartBlock.compareTo(startBlock);
    }

}
