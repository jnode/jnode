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

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 * 
 */
public class GroupDescriptor {
    public static final int GROUPDESCRIPTOR_LENGTH = 32;

    private byte data[];
    private Ext2FileSystem fs;
    private int groupNr;
    private boolean dirty;
    private final Logger log = Logger.getLogger(getClass());

    public GroupDescriptor() {
        data = new byte[GROUPDESCRIPTOR_LENGTH];
        log.setLevel(Level.INFO);
    }

    /*
     * create() and read() precedes any access to the inners of the group
     * descriptor, so no synchronization is needed
     */
    public void read(int groupNr, Ext2FileSystem fs) throws IOException {
        // read the group descriptors from the main copy in block group 0
        // byte[] blockData = fs.getBlock(
        // fs.getSuperblock().getFirstDataBlock() + 1);
        long baseBlock = fs.getSuperblock().getFirstDataBlock() + 1;
        long blockOffset = (groupNr * GROUPDESCRIPTOR_LENGTH) / fs.getBlockSize();
        long offset = (groupNr * GROUPDESCRIPTOR_LENGTH) % fs.getBlockSize();
        byte[] blockData = fs.getBlock(baseBlock + blockOffset);

        // data = new byte[GROUPDESCRIPTOR_LENGTH];
        System.arraycopy(blockData, (int) offset, data, 0, GROUPDESCRIPTOR_LENGTH);
        this.groupNr = groupNr;
        this.fs = fs;
        setDirty(false);
    }

    /*
     * create() and read() precedes any access to the inners of the group
     * descriptor, so no synchronization is needed
     */
    public void create(int groupNr, Ext2FileSystem fs) {
        this.fs = fs;
        this.groupNr = groupNr;

        long desc; // the length of the superblock and group descriptor copies
                    // in the block group
        if (!fs.groupHasDescriptors(groupNr))
            desc = 0;
        else
            desc =
                    1 + /* superblock */
                    Ext2Utils.ceilDiv(fs.getGroupCount() * GroupDescriptor.GROUPDESCRIPTOR_LENGTH,
                            fs.getBlockSize()); /* GDT */
        Superblock superblock = fs.getSuperblock();
        setBlockBitmap(superblock.getFirstDataBlock() + groupNr * superblock.getBlocksPerGroup() + desc);

        setInodeBitmap(getBlockBitmap() + 1);
        setInodeTable(getBlockBitmap() + 2);

        long inodeTableSize =
                Ext2Utils.ceilDiv(superblock.getINodesPerGroup() * INode.INODE_LENGTH, fs.getBlockSize());
        long blockCount;
        if (groupNr == fs.getGroupCount() - 1)
            blockCount =
                superblock.getBlocksCount() - 
                superblock.getBlocksPerGroup() * (fs.getGroupCount() - 1) - 
                superblock.getFirstDataBlock();
        else
            blockCount = superblock.getBlocksPerGroup();

        setFreeBlocksCount((int) (blockCount - desc /*
                                                     * superblock copy, GDT
                                                     * copies
                                                     */
                - 2 /* block and inode bitmaps */
                - inodeTableSize)); /* inode table */

        if (groupNr == 0)
            setFreeInodesCount((int) (superblock.getINodesPerGroup() -
                    superblock.getFirstInode() + 1));
        else
            setFreeInodesCount((int) (superblock.getINodesPerGroup()));

        setUsedDirsCount(0);
    }

    /**
     * Update all copies of a single group descriptor. (GroupDescriptors are
     * duplicated in some (or all) block groups: if a GroupDescriptor changes,
     * all copies have to be changed.)
     * 
     * The method is synchronized with all methods that modify the group
     * descriptor (to "this") to ensure that it is not modified until all copies
     * are written to disk
     */
    protected synchronized void updateGroupDescriptor() throws IOException {
        if (isDirty()) {
            log.debug("Updating groupdescriptor copies");
            Superblock superblock = fs.getSuperblock();
            for (int i = 0; i < fs.getGroupCount(); i++) {
                // check if there is a group descriptor table copy in the block
                // group
                if (!fs.groupHasDescriptors(i))
                    continue;

                long block = superblock.getFirstDataBlock() + 1 + superblock.getBlocksPerGroup() * i; 
                long pos = groupNr * GROUPDESCRIPTOR_LENGTH;
                block += pos / fs.getBlockSize();
                long offset = pos % fs.getBlockSize();
                byte[] blockData = fs.getBlock(block);
                // update the block with the new group descriptor
                System.arraycopy(data, 0, blockData, (int) offset, GROUPDESCRIPTOR_LENGTH);
                fs.writeBlock(block, blockData, true);
            }
            setDirty(false);
        }
    }

    public int size() {
        return GROUPDESCRIPTOR_LENGTH;
    }

    // this field is only written during format (so no synchronization issues
    // here)
    public long getBlockBitmap() {
        return Ext2Utils.get32(data, 0);
    }

    public void setBlockBitmap(long l) {
        Ext2Utils.set32(data, 0, l);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues
    // here)
    public long getInodeBitmap() {
        return Ext2Utils.get32(data, 4);
    }

    public void setInodeBitmap(long l) {
        Ext2Utils.set32(data, 4, l);
        setDirty(true);
    }

    // this field is only written during format (so no synchronization issues
    // here)
    public long getInodeTable() {
        return Ext2Utils.get32(data, 8);
    }

    public void setInodeTable(long l) {
        Ext2Utils.set32(data, 8, l);
        setDirty(true);
    }

    public synchronized int getFreeBlocksCount() {
        return Ext2Utils.get16(data, 12);
    }

    public synchronized void setFreeBlocksCount(int count) {
        Ext2Utils.set16(data, 12, count);
        setDirty(true);
    }

    public synchronized int getFreeInodesCount() {
        return Ext2Utils.get16(data, 14);
    }

    public synchronized void setFreeInodesCount(int count) {
        Ext2Utils.set16(data, 14, count);
        setDirty(true);
    }

    public synchronized int getUsedDirsCount() {
        return Ext2Utils.get16(data, 16);
    }

    public synchronized void setUsedDirsCount(int count) {
        Ext2Utils.set16(data, 16, count);
        setDirty(true);
    }

    /**
     * @return
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @param b
     */
    public void setDirty(boolean b) {
        dirty = b;
    }
}
