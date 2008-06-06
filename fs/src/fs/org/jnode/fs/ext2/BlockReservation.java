/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * Class used to return information to the INode when the FSBitmap makes a
 * reservation.
 * 
 * @author Andras Nagy
 */
public class BlockReservation {
    private long block;
    private int preallocCount;
    private boolean successful;
    private long freeBlocksCount;

    public BlockReservation(boolean successful, long block, int preallocCount) {
        this.successful = successful;
        this.block = block;
        this.preallocCount = preallocCount;
    }

    public BlockReservation(boolean successful, long block, int preallocCount, long freeBlocksCount) {
        this.successful = successful;
        this.block = block;
        this.preallocCount = preallocCount;
        this.freeBlocksCount = freeBlocksCount;
    }

    /**
     * Returns the block index (from the beginning of the partition).
     * 
     * @return long
     */
    public long getBlock() {
        return block;
    }

    /**
     * Returns the preallocCount.
     * 
     * @return int
     */
    public int getPreallocCount() {
        return preallocCount;
    }

    /**
     * Returns the successful.
     * 
     * @return boolean
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Sets the block.
     * 
     * @param block The block to set
     */
    public void setBlock(long block) {
        this.block = block;
    }

    /**
     * Returns how many free blocks are in the block group
     * 
     * @return
     */
    public long getFreeBlocksCount() {
        return freeBlocksCount;
    }

    /**
     * Sets how many free blocks are in the block group
     * @param l number of free blocks in the block group
     */
    public void setFreeBlocksCount(long l) {
        freeBlocksCount = l;
    }

}
