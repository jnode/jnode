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
public class INodeReservation {
    /**
     * The block group that contains the inode
     */
    private int group;

    /**
     * The index of the inode within the block group (begins with 0)
     */
    private int index;
    private boolean successful;

    /**
     * Results of an attempt to reserve an inode in a block group.
     * 
     * @param successful
     * @param index: begins at index 0 (shows the index in the inode bitmap and
     *            inode table). The actual inode number is
     *            <code>INODEX_PER_GROUP*groupNr + index + 1</code>, as
     *            inodes begin at 1 (this is what getInodeNr(groupNr) returns)
     */
    public INodeReservation(boolean successful, int index) {
        this.successful = successful;
        this.index = index;
    }

    public int getINodeNr(int iNodesPerGroup) {
        // iNodes start with 1
        return iNodesPerGroup * group + index + 1;
    }

    /**
     * Returns the successful.
     * 
     * @return boolean
     */
    protected boolean isSuccessful() {
        return successful;
    }

    /**
     * @return
     */
    public int getGroup() {
        return group;
    }

    /**
     * @param l
     */
    public void setGroup(int l) {
        group = l;
    }

    /**
     * @return
     */
    public int getIndex() {
        return index;
    }

}
