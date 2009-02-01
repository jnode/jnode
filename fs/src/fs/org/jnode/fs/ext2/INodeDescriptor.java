/*
 * $Id$
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
 
package org.jnode.fs.ext2;

import org.jnode.fs.FileSystemException;

/**
 * This class stores nonpersistent information about the inode.
 * It also contains a reference to the part of the inode table
 * which contains this inode.
 * 
 * @author Andras Nagy 
 */

public class INodeDescriptor {

    private int iNodeNr;

    /* The number of blocks allocated for the iNode is not necessary equal to its size in blocks:
     * <ul>
     * <li>if a block has been allocated but not yet been written
     * <li>if the file is in the process of being truncated, i.e. some blocks have already been 
     *     freed, but the new size has not yet been set.
     * </ul>
     * The lastAllocatedBlockIndex attribute is always the index of the last allocated block 
     */
    private long lastAllocatedBlockIndex = -1;

    //which block group contains the inode
    private int group;

    //the index of the inode in the block group
    private int index;

    //the inode table that contains this inode
    private INodeTable iNodeTable;

    //preallocated blocks are maintained as long as the inode is in memory.
    //When it is flushed(), unused preallocated blocks must be freed in the 
    //block bitmap.
    private long preallocBlock;

    private int preallocCount;

    public synchronized long usePreallocBlock() throws FileSystemException {
        if (preallocCount <= 0) {
            throw new FileSystemException("No preallocated blocks");
        }
        --preallocCount;
        return preallocBlock++;
    }

    public INodeDescriptor(INodeTable iNodeTable, int iNodeNr, int group, int index) {
        this.iNodeTable = iNodeTable;
        this.iNodeNr = iNodeNr;
        this.group = group;
        this.index = index;
        preallocCount = 0;
    }

    public int getPreallocCount() {
        return preallocCount;
    }

    public synchronized void setPreallocBlock(long l) {
        preallocBlock = l;
    }

    public synchronized void setPreallocCount(int l) {
        preallocCount = l;
    }

    /**
     * Returns in which block group the inode is placed
     * @return int
     */
    public int getGroup() {
        return group;
    }

    /**
     * Returns the index of the inode in its inode table (there is an inode
     * which block group the inode is placed
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the inode table that contains this inode
     * @return the INode table
     */
    public INodeTable getINodeTable() {
        return iNodeTable;
    }

    public long getLastAllocatedBlockIndex() {
        return lastAllocatedBlockIndex;
    }

    public void setLastAllocatedBlockIndex(long l) {
        lastAllocatedBlockIndex = l;
    }

    public int getINodeNr() {
        return iNodeNr;
    }
}

