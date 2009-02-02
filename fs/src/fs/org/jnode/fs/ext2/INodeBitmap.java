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
 
package org.jnode.fs.ext2;

/**
 * @author Andras Nagy
 */
public class INodeBitmap extends FSBitmap {
    /**
     * Test whether the inode is free, and if yes, mark it as used
     * 
     * SYNCHRONIZATION:
     * INodeBitmap.testAndSetINode() is not synchronized, so 
     * Ext2FileSystem.createINode() is synchronized to the bitmap block
     * it operates on.
     */
    protected static boolean testAndSetINode(byte[] data, int index) {
        if (isFree(data, index)) {
            setBit(data, index);
            return true;
        } else {
            return false;
        }
    }

    public static INodeReservation findFreeINode(byte[] bitmapBlock) {
        for (int i = 0; i < bitmapBlock.length; i++) {
            if (bitmapBlock[i] != 0xFF) {
                for (int j = 0; j < 8; j++) {
                    if (isFree(bitmapBlock[i], j)) {
                        setBit(bitmapBlock, i, j);
                        return new INodeReservation(true, i * 8 + j);
                    }
                }
            }
        }
        return new INodeReservation(false, -1);
    }

}
