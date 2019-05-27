/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ext2.exception.UnallocatedBlockException;
import org.jnode.fs.ext2.xattr.XAttrEntry;
import org.jnode.fs.ext2.xattr.XAttrHeader;
import org.jnode.fs.ext2.xattr.XAttrInlineEntry;
import org.jnode.fs.ext4.ExtentHeader;
import org.jnode.fs.util.FSUtils;
import org.jnode.util.LittleEndian;

/**
 * This class represents an inode. Once they are allocated, inodes are read and
 * written by the INodeTable (which is accessible through desc.getINodeTable().
 *
 * @author Andras Nagy
 */
public class INode {
    public static final int EXT2_GOOD_OLD_INODE_SIZE = 128;

    private static final Logger log = Logger.getLogger(INode.class);

    /**
     * the data constituting the inode itself
     */
    private byte[] data;

    private volatile boolean dirty;

    /**
     * If an inode is locked, it may not be flushed from the cache (locked
     * counts the number of threads that have locked the inode)
     */
    private volatile int locked;

    /**
     * nonpersistent data stored in memory only
     */
    INodeDescriptor desc = null;

    private Ext2FileSystem fs;

    /**
     * The cached extent header.
     */
    private ExtentHeader extentHeader;

    /**
     * Create an INode object from an existing inode on the disk.
     *
     * @param fs
     * @param desc
     */
    public INode(Ext2FileSystem fs, INodeDescriptor desc) {
        this.fs = fs;
        this.desc = desc;
        this.data = new byte[fs.getSuperblock().getINodeSize()];
        locked = 0;
    }

    public void read(byte[] data) {
        System.arraycopy(data, 0, this.data, 0, fs.getSuperblock().getINodeSize());
        setDirty(false);
    }

    /**
     * Create a new INode object from scratch
     */
    public void create(int fileFormat, int accessRights, int uid, int gid) {
        long time = System.currentTimeMillis() / 1000;
        log.debug("TIME:                " + time);

        setUid(uid);
        setGid(gid);
        setMode(fileFormat | accessRights);
        setSize(0);
        setAtime(time);
        setCtime(time);
        setMtime(time);
        setDtime(0);
        setLinksCount(0);
        //TODO: set other persistent parameters?

        setDirty(true);
    }

    public long getINodeNr() {
        return desc.getINodeNr();
    }

    protected void finalize() throws Exception {
        flush();
    }

    /**
     * Called when an inode is flushed from the inode buffer and its state must
     * be saved to the disk
     */
    public void flush() throws IOException, FileSystemException {
        log.debug("Flush called for inode " + getINodeNr());

        freePreallocatedBlocks();
        update();
    }

    /**
     * write an inode back to disk
     *
     * @throws IOException synchronize to avoid that half-set fields get written to the inode
     */
    protected synchronized void update() throws IOException {
        try {
            if (dirty) {
                log.debug("  ** updating inode **");
                desc.getINodeTable().writeInodeData(desc.getIndex(), data);
                dirty = false;
            }
        } catch (FileSystemException ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Return the number of the group that contains the inode.
     *
     * @return the group number
     */
    protected long getGroup() {
        return desc.getGroup();
    }

    public Ext2FileSystem getExt2FileSystem() {
        return fs;
    }

    /**
     * Gets the extra inode size.
     *
     * @return the extra size.
     */
    public int getExtraISize() {
        if (getExt2FileSystem().hasROFeature(Ext2Constants.EXT4_FEATURE_RO_COMPAT_EXTRA_ISIZE) && data.length > 0x82) {
            return LittleEndian.getInt16(data, 0x80);
        }

        // Extra isize not supported
        return 0;
    }

    /**
     * Gets the extra attribute block.
     *
     * @return the extra attribute block.
     */
    public long getXAttrBlock() {
        long blockLow = LittleEndian.getUInt32(data, 0x68);
        long blockHigh = LittleEndian.getUInt16(data, 0x76);
        return blockLow | blockHigh << 32;
    }

    /**
     * Gets a list of attributes associated with this inode.
     *
     * @return the list of attributes.
     */
    public List<XAttrEntry> getAttributes() {
        List<XAttrEntry> attributes = new ArrayList<XAttrEntry>();
        attributes.addAll(getInlineAttributes());
        attributes.addAll(getAttributesInBlock());
        return attributes;
    }

    /**
     * Gets an attribute by its name.
     *
     * @param name the name of the attribute to look up.
     * @return the attribute, or {@code null} if no match is found.
     */
    public XAttrEntry getAttribute(String name) {
        for (XAttrEntry attribute : getAttributes()) {
            if (name.equals(attribute.getName())) {
                return attribute;
            }
        }

        return null;
    }

    /**
     * Gets a list of inline attributes associated with this inode.
     *
     * @return the list of attributes.
     */
    public List<XAttrEntry> getInlineAttributes() {
        List<XAttrEntry> attributes = new ArrayList<XAttrEntry>();
        int inodeSize = getExt2FileSystem().getSuperblock().getINodeSize();
        int xattrAvailableSize = inodeSize - (EXT2_GOOD_OLD_INODE_SIZE + getExtraISize());

        if (xattrAvailableSize > 0) {
            byte[] xattrBuffer = new byte[xattrAvailableSize];

            System.arraycopy(data, EXT2_GOOD_OLD_INODE_SIZE + getExtraISize(), xattrBuffer, 0, xattrAvailableSize);
            XAttrHeader xAttrHeader = new XAttrHeader(xattrBuffer);

            if (xAttrHeader.getMagic() == XAttrHeader.MAGIC) {
                for (int offset = 4; offset + XAttrEntry.MINIMUM_SIZE < xattrBuffer.length; ) {
                    if (LittleEndian.getUInt32(xattrBuffer, offset) == 0) {
                        break;
                    }

                    XAttrEntry entry = new XAttrInlineEntry(xattrBuffer, offset);
                    attributes.add(entry);

                    offset += FSUtils.roundUpToBoundary(4, entry.getNameLength() + XAttrEntry.MINIMUM_SIZE);
                }
            }
        }

        return attributes;
    }

    /**
     * Gets a list of attributes stored off in the extra attribute block.
     *
     * @return the list of attributes.
     */
    public List<XAttrEntry> getAttributesInBlock() {
        List<XAttrEntry> attributes = new ArrayList<XAttrEntry>();
        long xAttrBlockNumber = getXAttrBlock();

        if (xAttrBlockNumber != 0) {
            try {
                byte[] xattrBuffer = fs.getBlock(xAttrBlockNumber);
                XAttrHeader xAttrHeader = new XAttrHeader(xattrBuffer);

                if (xAttrHeader.getMagic() == XAttrHeader.MAGIC) {
                    for (int offset = XAttrHeader.SIZE; offset + XAttrEntry.MINIMUM_SIZE < xattrBuffer.length; ) {
                        if (LittleEndian.getUInt32(xattrBuffer, offset) == 0) {
                            break;
                        }

                        XAttrEntry entry = new XAttrEntry(xattrBuffer, offset);
                        attributes.add(entry);

                        offset += FSUtils.roundUpToBoundary(4, entry.getNameLength() + XAttrEntry.MINIMUM_SIZE);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading extra attributes block: " + xAttrBlockNumber, e);
            }
        }

        return attributes;
    }

    /**
     * return the number of direct blocks that an indirect block can point to
     *
     * @return the count
     */
    private final int getIndirectCount() {
        return fs.getSuperblock().getBlockSize() >> 2; //a block index is 4
        // bytes long
    }

    /**
     * Parse the indirect blocks of level <code>indirectionLevel</code> and
     * return the address of the <code>offset</code> th block. For example,
     * indirectRead( doubleIndirectBlockNumber, 45, 3) will return the 45th
     * block that is reachable via triple indirection, which is the (12 +
     * getIndirectCount() +getIndirectCount()^2 + 45)th block of the inode (12
     * direct blocks, getIndirectCount() simple indirect blocks,
     * getIndirectCount()^2 double indirect blocks, 45th triple indirect block).
     *
     * @param indirectionLevel 0 is a direct block, 1 is a simple indirect block, and
     *                         so on.
     */
    private final long indirectRead(long dataBlockNr, long offset, int indirectionLevel)
        throws IOException {
        byte[] data = fs.getBlock(dataBlockNr);
        if (indirectionLevel == 1)
            //data is a (simple) indirect block
            return LittleEndian.getUInt32(data, (int) offset * 4);

        long blockIndex = offset / (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        long blockOffset = offset % (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        long blockNr = LittleEndian.getUInt32(data, (int) blockIndex * 4);

        return indirectRead(blockNr, blockOffset, indirectionLevel - 1);
    }

    /**
     * Parse the indirect blocks of level <code>indirectionLevel</code> and
     * register the address of the <code>offset</code> th block. Also see
     * indirectRead().
     *
     * @param allocatedBlocks (the number of blocks allocated so far)-1
     */
    private final void indirectWrite(long dataBlockNr, long offset, long allocatedBlocks,
                                     long value, int indirectionLevel) throws IOException, FileSystemException {
        log.debug("indirectWrite(blockNr=" + dataBlockNr + ", offset=" + offset + "...)");
        byte[] data = fs.getBlock(dataBlockNr);
        if (indirectionLevel == 1) {
            //data is a (simple) indirect block
            Ext2Utils.set32(data, (int) offset * 4, value);
            //write back the updated block
            fs.writeBlock(dataBlockNr, data, false);
            return;
        }

        long blockNr;
        long blockIndex = offset / (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        long blockOffset = offset % (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        if (blockOffset == 0) {
            //need to reserve the indirect block itself
            blockNr = findFreeBlock(allocatedBlocks++);
            Ext2Utils.set32(data, (int) blockIndex * 4, blockNr);
            fs.writeBlock(dataBlockNr, data, false);

            //need to blank the block so that e2fsck does not complain
            byte[] zeroes = new byte[fs.getBlockSize()]; //blank the block
            Arrays.fill(zeroes, 0, fs.getBlockSize(), (byte) 0);
            fs.writeBlock(blockNr, zeroes, false);
        } else {
            blockNr = LittleEndian.getUInt32(data, (int) blockIndex * 4);
        }

        indirectWrite(blockNr, blockOffset, allocatedBlocks, value, indirectionLevel - 1);
    }

    /**
     * Free up block dataBlockNr, and free up any indirect blocks, if needed
     *
     * @param dataBlockNr
     * @param offset
     * @param indirectionLevel
     * @throws IOException
     */
    private final void indirectFree(long dataBlockNr, long offset, int indirectionLevel)
        throws IOException, FileSystemException {
        log.debug("indirectFree(datablockNr=" + dataBlockNr + ", offset=" + offset + ", ind=" +
            indirectionLevel + ")");
        if (indirectionLevel == 0) {
            fs.freeBlock(dataBlockNr);
            return;
        }

        byte[] data = fs.getBlock(dataBlockNr);

        long blockIndex = offset / (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        long blockOffset = offset % (long) Math.pow(getIndirectCount(), indirectionLevel - 1);
        long blockNr = LittleEndian.getUInt32(data, (int) blockIndex * 4);

        indirectFree(blockNr, blockOffset, indirectionLevel - 1);

        if (offset == 0) {
            //block blockNr has been the last block pointer on the indirect
            // block,
            //so the indirect block can be freed up as well
            fs.freeBlock(dataBlockNr);
            long block512 = fs.getBlockSize() / 512;
            setBlocks(getBlocks() - block512);
        }
    }

    /**
     * Gets the data stored inline in the inode's i_block element.
     *
     * @return the inode block data.
     */
    public byte[] getINodeBlockData() {
        byte[] buffer = new byte[64];
        System.arraycopy(data, 40, buffer, 0, buffer.length);
        return buffer;
    }

    /**
     * Return the number of the block in the filesystem that stores the ith
     * block of the inode (i is a sequential index from the beginning of the
     * file)
     * <p/>
     * [Naming convention used: in the code, a <code>...BlockNr</code> always
     * means an absolute block nr (of the filesystem), while a
     * <code>...BlockIndex</code> means an index relative to the beginning of
     * a block]
     *
     * @param i
     * @return the block number
     * @throws IOException
     */
    public long getDataBlockNr(long i) throws IOException {
        if ((getFlags() & Ext2Constants.EXT4_INODE_EXTENTS_FLAG) != 0) {
            if (extentHeader == null) {
                extentHeader = new ExtentHeader(getINodeBlockData());
            }

            return extentHeader.getBlockNumber(fs, i);
        } else {
            return getDataBlockNrIndirect(i);
        }
    }

    /**
     * Return the number of the block in the filesystem that stores the ith
     * block of the inode (i is a sequential index from the beginning of the
     * file) using an indirect (ext2 / ext3) lookup.
     * <p/>
     * [Naming convention used: in the code, a <code>...BlockNr</code> always
     * means an absolute block nr (of the filesystem), while a
     * <code>...BlockIndex</code> means an index relative to the beginning of
     * a block]
     *
     * @param i
     * @return the block number
     * @throws IOException
     */
    private long getDataBlockNrIndirect(long i) throws IOException {
        final long blockCount = getAllocatedBlockCount();
        final int indirectCount = getIndirectCount();
        if (i > blockCount - 1) {
            throw new IOException("Trying to read block " + i + " (counts from 0), while" +
                " INode contains only " + blockCount + " blocks");
        }

        //get the direct blocks (0; 11)
        if (i < 12) {
            log.debug("getDataBlockNr(): block nr: " + LittleEndian.getUInt32(data, 40 + (int) i * 4));
            return LittleEndian.getUInt32(data, 40 + (int) i * 4);
        }

        //see the indirect blocks (12; indirectCount-1)
        i -= 12;
        if (i < indirectCount) {
            //the 12th index points to the indirect block
            return indirectRead(LittleEndian.getUInt32(data, 40 + 12 * 4), i, 1);
        }

        //see the double indirect blocks (indirectCount; doubleIndirectCount-1)
        i -= indirectCount;
        if (i < indirectCount * indirectCount) {
            //the 13th index points to the double indirect block
            return indirectRead(LittleEndian.getUInt32(data, 40 + 13 * 4), i, 2);
        }

        //see the triple indirect blocks (doubleIndirectCount;
        // tripleIndirectCount-1)
        i -= indirectCount * indirectCount;
        if (i < indirectCount * indirectCount * indirectCount) {
            //the 14th index points to the triple indirect block
            return indirectRead(LittleEndian.getUInt32(data, 40 + 14 * 4), i, 3);
        }

        //shouldn't get here
        throw new IOException("Internal FS exception: getDataBlockIndex(i=" + i + ")");
    }

    /**
     * Read the ith block of the inode (i is a sequential index from the
     * beginning of the file, and not an absolute block number)
     *
     * @param i
     * @return the data block
     * @throws IOException
     */
    public byte[] getDataBlock(long i) throws IOException {
        return fs.getBlock(getDataBlockNr(i));
    }

    /**
     * A new block has been allocated for the inode, so register it (the
     * <code>i</code> th block of the inode is the block at
     * <code>blockNr</code>
     * <p/>
     * [Naming convention used: in the code, a <code>...BlockNr</code> always
     * means an absolute block nr (of the filesystem), while a
     * <code>...BlockIndex</code> means an index relative to the beginning of
     * a block]
     *
     * @param i       the ith block of the inode has been reserved
     * @param blockNr the block (in the filesystem) that has been reserved
     */
    private final void registerBlockIndex(long i, long blockNr)
        throws FileSystemException, IOException {
        final long blockCount = getSizeInBlocks();
        final int indirectCount = getIndirectCount();
        long allocatedBlocks = i;
        if (i != blockCount) {
            throw new FileSystemException("Trying to register block " + i +
                " (counts from 0), when INode contains only " + blockCount + " blocks");
        }

        log.debug("registering block #" + blockNr);

        setDirty(true);

        //the direct blocks (0; 11)
        if (i < 12) {
            Ext2Utils.set32(data, 40 + (int) i * 4, blockNr);
            return;
        }

        //see the indirect blocks (12; indirectCount-1)
        i -= 12;
        if (i < indirectCount) {
            long indirectBlockNr;
            //the 12th index points to the indirect block
            if (i == 0) {
                //need to reserve the indirect block itself, as this is the
                //first time it is used
                indirectBlockNr = findFreeBlock(allocatedBlocks++);
                Ext2Utils.set32(data, 40 + 12 * 4, indirectBlockNr);

                //log.debug("reserved indirect block: "+indirectBlockNr);

                //need to blank the block so that e2fsck does not complain
                byte[] zeroes = new byte[fs.getBlockSize()]; //blank the block
                Arrays.fill(zeroes, 0, fs.getBlockSize(), (byte) 0);
                fs.writeBlock(indirectBlockNr, zeroes, false);
            } else {
                //the indirect block has already been used
                indirectBlockNr = LittleEndian.getUInt32(data, 40 + 12 * 4);
            }

            indirectWrite(indirectBlockNr, i, allocatedBlocks, blockNr, 1);

            return;
        }

        //see the double indirect blocks (indirectCount; doubleIndirectCount-1)
        i -= indirectCount;
        final int doubleIndirectCount = indirectCount * indirectCount;
        if (i < doubleIndirectCount) {
            long doubleIndirectBlockNr;
            //the 13th index points to the double indirect block
            if (i == 0) {
                //need to reserve the double indirect block itself
                doubleIndirectBlockNr = findFreeBlock(allocatedBlocks++);
                Ext2Utils.set32(data, 40 + 13 * 4, doubleIndirectBlockNr);

                //log.debug("reserved double indirect block:
                // "+doubleIndirectBlockNr);

                //need to blank the block so that e2fsck does not complain
                byte[] zeroes = new byte[fs.getBlockSize()]; //blank the block
                Arrays.fill(zeroes, 0, fs.getBlockSize(), (byte) 0);
                fs.writeBlock(doubleIndirectBlockNr, zeroes, false);
            } else {
                doubleIndirectBlockNr = LittleEndian.getUInt32(data, 40 + 13 * 4);
            }

            indirectWrite(doubleIndirectBlockNr, i, allocatedBlocks, blockNr, 2);

            return;
        }

        //see the triple indirect blocks (doubleIndirectCount;
        // tripleIndirectCount-1)
        final int tripleIndirectCount = indirectCount * indirectCount * indirectCount;
        i -= doubleIndirectCount;
        if (i < tripleIndirectCount) {
            long tripleIndirectBlockNr;
            //the 14th index points to the triple indirect block
            if (i == 0) {
                //need to reserve the triple indirect block itself
                tripleIndirectBlockNr = findFreeBlock(allocatedBlocks++);
                Ext2Utils.set32(data, 40 + 13 * 4, tripleIndirectBlockNr);

                //log.debug("reserved triple indirect block:
                // "+tripleIndirectBlockNr);

                //need to blank the block so that e2fsck does not complain
                byte[] zeroes = new byte[fs.getBlockSize()]; //blank the block
                Arrays.fill(zeroes, 0, fs.getBlockSize(), (byte) 0);
                fs.writeBlock(tripleIndirectBlockNr, zeroes, false);
            } else {
                tripleIndirectBlockNr = LittleEndian.getUInt32(data, 40 + 14 * 4);
            }

            indirectWrite(tripleIndirectBlockNr, i, allocatedBlocks, blockNr, 3);
            return;
        }

        //shouldn't get here
        throw new FileSystemException("Internal FS exception: getDataBlockIndex(i=" + i + ")");
    }

    /**
     * Free the preallocated blocks
     *
     * @throws FileSystemException
     * @throws IOException
     */
    private void freePreallocatedBlocks() throws FileSystemException, IOException {
        int preallocCount = desc.getPreallocCount();
        if (preallocCount > 0) {
            log.debug("Freeing preallocated blocks");
        } else {
            log.debug("No preallocated blocks in the inode");
            return;
        }

        long prealloc512 = preallocCount * (fs.getBlockSize() / 512);
        setBlocks(getBlocks() - prealloc512);

        while (desc.getPreallocCount() > 0) {
            fs.freeBlock(desc.usePreallocBlock());
        }
    }

    /**
     * Free up the ith data block of the inode. It is neccessary to free up
     * indirect blocks as well, if the last pointer on an indirect block has
     * been freed.
     *
     * @param i
     * @throws IOException
     */
    protected synchronized void freeDataBlock(long i) throws IOException, FileSystemException {
        final long blockCount = getAllocatedBlockCount();
        final int indirectCount = getIndirectCount();

        if (i != blockCount - 1) {
            throw new IOException("Only the last block of the inode can be freed." +
                "You were trying to free block nr. " + i + ", while inode contains " +
                blockCount + " blocks.");
        }

        desc.setLastAllocatedBlockIndex(i - 1);

        //preallocated blocks follow the last allocated block: when the last
        // block is freed,
        //free the preallocated blocks as well
        freePreallocatedBlocks();

        long block512 = fs.getBlockSize() / 512;
        setBlocks(getBlocks() - block512);

        setDirty(true);

        //see the direct blocks (0; 11)
        if (i < 12) {
            indirectFree(LittleEndian.getUInt32(data, 40 + (int) i * 4), 0, 0);
            Ext2Utils.set32(data, 40 + (int) i * 4, 0);
            return;
        }

        //see the indirect blocks (12; indirectCount-1)
        i -= 12;
        if (i < indirectCount) {
            //the 12th index points to the indirect block
            indirectFree(LittleEndian.getUInt32(data, 40 + 12 * 4), i, 1);
            //if this was the last block on the indirect block, then delete the
            // record of
            //the indirect block from the inode
            if (i == 0) {
                Ext2Utils.set32(data, 40 + 12 * 4, 0);
            }
            return;
        }

        //see the double indirect blocks (indirectCount; doubleIndirectCount-1)
        i -= indirectCount;
        if (i < indirectCount * indirectCount) {
            //the 13th index points to the double indirect block
            indirectFree(LittleEndian.getUInt32(data, 40 + 13 * 4), i, 2);
            //if this was the last block on the double indirect block, then
            // delete the record of
            //the double indirect block from the inode
            if (i == 0) {
                Ext2Utils.set32(data, 40 + 13 * 4, 0);
            }
            return;
        }

        //see the triple indirect blocks (doubleIndirectCount;
        // tripleIndirectCount-1)
        i -= indirectCount * indirectCount;
        if (i < indirectCount * indirectCount * indirectCount) {
            //the 14th index points to the triple indirect block
            indirectFree(LittleEndian.getUInt32(data, 40 + 14 * 4), i, 3);
            //if this was the last block on the triple indirect block, then
            // delete the record of
            //the triple indirect block from the inode
            if (i == 0) {
                Ext2Utils.set32(data, 40 + 14 * 4, 0);
            }
            return;
        }

        //shouldn't get here
        throw new IOException("Internal FS exception: getDataBlockIndex(i=" + i + ")");
    }

    /**
     * Write the i. data block of the inode (i is a sequential index from the
     * beginning of the file, and not an absolute block number)
     * <p/>
     * This method assumes that the block has already been reserved.
     *
     * @param i
     * @param data
     */
    public void writeDataBlock(long i, byte[] data) throws IOException {
        //see if the block is already reserved for the inode
        long blockCount = getAllocatedBlockCount();

        if (i < blockCount) {
            long blockIndex = getDataBlockNr(i);
            //overwrite the block
            fs.writeBlock(blockIndex, data, false);
        } else {
            throw new UnallocatedBlockException("Block " + i + " not yet reserved " +
                "for the inode");
        }
    }

    /**
     * Get the number of blocks allocated so far for the inode. It is possible
     * that a new block has been allocated, but not yet been written to. In this
     * case, it is not counted by getSizeInBlocks(), because it returns the size
     * of the file in blocks, counting only written bytes
     *
     * @return the count
     */
    protected long getAllocatedBlockCount() {
        if (desc.getLastAllocatedBlockIndex() != -1) {
            return desc.getLastAllocatedBlockIndex() + 1;
        } else {
            return getSizeInBlocks();
        }
    }

    /**
     * Allocate the ith data block of the inode (i is a sequential index from
     * the beginning of the file, and not an absolute block number)
     *
     * @param i
     */
    public synchronized void allocateDataBlock(long i) throws FileSystemException, IOException {
        if (i < getAllocatedBlockCount()) {
            throw new IOException(i + " blocks are already allocated for this inode");
        }
        if (i > getAllocatedBlockCount()) {
            throw new IOException("Allocate block " + getAllocatedBlockCount() + " first!");
        }

        long newBlock = findFreeBlock(i);

        log.debug("Allocated new block " + newBlock);

        desc.setLastAllocatedBlockIndex(i);

        registerBlockIndex(i, newBlock);
    }

    /**
     * FINDS a free block which will be the indexth block of the inode: -first
     * check the preallocated blocks -then check around the last allocated block
     * and ALLOCATES it in the block bitmap at the same time.
     * <p/>
     * Block allocation should be contiguous if possible, i.e. the new block
     * should be the one that follows the last allocated block (that's why the
     * <code>index</code> parameter is needed).
     *
     * @param index the block to be found should be around the (index-1)th block
     *              of the inode (which is already allocated, unless index==0)
     */
    private long findFreeBlock(long index) throws IOException, FileSystemException {
        //long newBlock;
        long lastBlock = -1;
        BlockReservation reservation;

        //first, see if preallocated blocks exist
        if (desc.getPreallocCount() > 0) {
            return desc.usePreallocBlock();
        }

        //no preallocated blocks:
        //check around the last allocated block
        if (index > 0)
            lastBlock = getDataBlockNr(index - 1);
        if (lastBlock != -1) {
            for (int i = 1; i < 16; i++) {
                reservation = getExt2FileSystem().testAndSetBlock(lastBlock + i);
                if (reservation.isSuccessful()) {
                    desc.setPreallocBlock(reservation.getBlock() + 1);
                    desc.setPreallocCount(reservation.getPreallocCount());

                    long prealloc512 =
                        (1 + reservation.getPreallocCount()) * (fs.getBlockSize() / 512);
                    setBlocks(getBlocks() + prealloc512);

                    return lastBlock + i;
                }
            }

            for (int i = -15; i < 0; i++) {
                reservation = getExt2FileSystem().testAndSetBlock(lastBlock + i);
                if (reservation.isSuccessful()) {
                    desc.setPreallocBlock(reservation.getBlock() + 1);
                    desc.setPreallocCount(reservation.getPreallocCount());

                    long prealloc512 =
                        (1 + reservation.getPreallocCount()) * (fs.getBlockSize() / 512);
                    setBlocks(getBlocks() + prealloc512);

                    return lastBlock + i;
                }
            }
        }

        //then check the current block group from the beginning
        //(threshold=1 means: find is successul if at least one free block is
        // found)
        reservation = getExt2FileSystem().findFreeBlocks(desc.getGroup(), 1);
        if (reservation.isSuccessful()) {
            desc.setPreallocBlock(reservation.getBlock() + 1);
            desc.setPreallocCount(reservation.getPreallocCount());

            long prealloc512 = (1 + reservation.getPreallocCount()) * (fs.getBlockSize() / 512);
            setBlocks(getBlocks() + prealloc512);

            return reservation.getBlock();
        }

        //then check the other block groups, first those that have "more" free
        // space,
        //but take a note if a non-full group is found
        long nonfullBlockGroup = -1;
        for (int i = 0; i < getExt2FileSystem().getGroupCount(); i++) {
            if (i == desc.getGroup()) {
                continue;
            }
            long threshold =
                (getExt2FileSystem().getSuperblock().getBlocksPerGroup() *
                    Ext2Constants.EXT2_BLOCK_THRESHOLD_PERCENT) / 100;
            reservation = getExt2FileSystem().findFreeBlocks(i, threshold);
            if (reservation.isSuccessful()) {
                desc.setPreallocBlock(reservation.getBlock() + 1);
                desc.setPreallocCount(reservation.getPreallocCount());

                long prealloc512 = (1 + reservation.getPreallocCount()) * (fs.getBlockSize() / 512);
                setBlocks(getBlocks() + prealloc512);

                return reservation.getBlock();
            }

            if (reservation.getFreeBlocksCount() > 0) {
                nonfullBlockGroup = i;
            }
        }

        //if no block group with at least the threshold number of free blocks
        // is found,
        //then check if there was any nonfull group
        if (nonfullBlockGroup != -1) {
            reservation = getExt2FileSystem().findFreeBlocks(desc.getGroup(), 1);
            if (reservation.isSuccessful()) {
                desc.setPreallocBlock(reservation.getBlock() + 1);
                desc.setPreallocCount(reservation.getPreallocCount());

                long prealloc512 = (1 + reservation.getPreallocCount()) * (fs.getBlockSize() / 512);
                setBlocks(getBlocks() + prealloc512);

                return reservation.getBlock();
            }
        }

        throw new IOException("No free blocks: disk full!");
    }

    // **************** other persistent inode data *******************
    public synchronized int getMode() {
        int iMode = LittleEndian.getUInt16(data, 0);
        //log.debug("INode.getIMode(): "+Ext2Print.hexFormat(iMode));
        return iMode;
    }

    public synchronized void setMode(int imode) {
        LittleEndian.setInt16(data, 0, imode);
        setDirty(true);
    }

    public synchronized int getUid() {
        return LittleEndian.getUInt16(data, 2);
    }

    public synchronized void setUid(int uid) {
        LittleEndian.setInt16(data, 2, uid);
        setDirty(true);
    }

    /**
     * Return the size of the file in bytes.
     *
     * @return the size of the file in bytes
     */
    public synchronized long getSize() {
        long sizeLow = LittleEndian.getUInt32(data, 4);
        long sizeHigh = LittleEndian.getUInt32(data, 0x6C);

        if ((getFlags() & Ext2Constants.EXT4_HUGE_FILE_FL) != 0) {
            return (sizeHigh + sizeLow) << 32;
        } else {
            sizeHigh = sizeHigh << 32;
            return sizeHigh | sizeLow;
        }
    }

    public synchronized void setSize(long size) {
        Ext2Utils.set32(data, 4, size);
        setDirty(true);
    }

    /**
     * Return the size in ext2-blocks (getBlocks() returns the size in 512-byte
     * blocks, but an ext2 block can be of different size).
     *
     * @return the size
     */
    public long getSizeInBlocks() {
        return Ext2Utils.ceilDiv(getSize(), getExt2FileSystem().getBlockSize());
    }

    public synchronized long getAtime() {
        return LittleEndian.getUInt32(data, 8);
    }

    public synchronized void setAtime(long atime) {
        Ext2Utils.set32(data, 8, atime);
        setDirty(true);
    }

    public synchronized long getCtime() {
        return LittleEndian.getUInt32(data, 12);
    }

    public synchronized void setCtime(long ctime) {
        Ext2Utils.set32(data, 12, ctime);
        setDirty(true);
    }

    public synchronized long getMtime() {
        return LittleEndian.getUInt32(data, 16);
    }

    public synchronized void setMtime(long mtime) {
        Ext2Utils.set32(data, 16, mtime);
        setDirty(true);
    }

    public synchronized long getDtime() {
        return LittleEndian.getUInt32(data, 20);
    }

    public synchronized void setDtime(long dtime) {
        Ext2Utils.set32(data, 20, dtime);
        setDirty(true);
    }

    public synchronized int getGid() {
        return LittleEndian.getUInt16(data, 24);
    }

    public synchronized void setGid(int gid) {
        LittleEndian.setInt16(data, 24, gid);
        setDirty(true);
    }

    public synchronized int getLinksCount() {
        return LittleEndian.getUInt16(data, 26);
    }

    public synchronized void setLinksCount(int lc) {
        LittleEndian.setInt16(data, 26, lc);
        setDirty(true);
    }

    /**
     * Return the size in 512-byte blocks.
     */
    public synchronized long getBlocks() {
        return LittleEndian.getUInt32(data, 28);
    }

    public synchronized void setBlocks(long count) {
        log.debug("setBlocks(" + count + ")");
        Ext2Utils.set32(data, 28, count);
        setDirty(true);
    }

    //this value is set by setSize

    public synchronized long getFlags() {
        return LittleEndian.getUInt32(data, 32);
    }

    public synchronized void setFlags(long flags) {
        Ext2Utils.set32(data, 32, flags);
        setDirty(true);
    }

    public synchronized long getOSD1() {
        return LittleEndian.getUInt32(data, 36);
    }

    public synchronized void setOSD1(long osd1) {
        Ext2Utils.set32(data, 36, osd1);
        setDirty(true);
    }

    public synchronized long getGeneration() {
        return LittleEndian.getUInt32(data, 100);
    }

    public synchronized void setGeneration(long gen) {
        Ext2Utils.set32(data, 100, gen);
        setDirty(true);
    }

    public synchronized long getFileACL() {
        return LittleEndian.getUInt32(data, 104);
    }

    public synchronized void setFileACL(long acl) {
        Ext2Utils.set32(data, 104, acl);
        setDirty(true);
    }

    public synchronized long getDirACL() {
        return LittleEndian.getUInt32(data, 108);
    }

    public synchronized void setDirACL(long acl) {
        Ext2Utils.set32(data, 108, acl);
        setDirty(true);
    }

    public synchronized long getFAddr() {
        return LittleEndian.getUInt32(data, 112);
    }

    public synchronized void setFAddr(long faddr) {
        Ext2Utils.set32(data, 112, faddr);
        setDirty(true);
    }

    //TODO: return OSD2 fields (12 bytes from offset 116)

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean b) {
        dirty = b;

        if (dirty) {
            extentHeader = null;
        }
    }

    public synchronized boolean isLocked() {
        return locked > 0;
    }

    public synchronized void incLocked() {
        ++locked;
    }

    public synchronized void decLocked() {
        --locked;
        if (locked == 0) {
            this.notifyAll();
        }
        if (locked < 0) {
            //What!??
            locked = 0;
            throw new RuntimeException("INode has been unlocked more than locked");
        }
    }
}
