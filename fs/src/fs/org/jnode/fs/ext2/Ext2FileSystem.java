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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.ext2.cache.Block;
import org.jnode.fs.ext2.cache.BlockCache;
import org.jnode.fs.ext2.cache.INodeCache;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Andras Nagy
 *  
 */
public class Ext2FileSystem extends AbstractFileSystem {
    private Superblock superblock;

    private GroupDescriptor groupDescriptors[];

    private INodeTable iNodeTables[];

    private int groupCount;

    private BlockCache blockCache;

    private INodeCache inodeCache;

    private final Logger log = Logger.getLogger(getClass());

    //private Object groupDescriptorLock;
    //private Object superblockLock;

    //private final boolean DEBUG=true;

    //TODO: SYNC_WRITE should be made a parameter
    /** if true, writeBlock() does not return until the block is written to disk */
    private boolean SYNC_WRITE = true;

    /**
     * Constructor for Ext2FileSystem in specified readOnly mode
     * 
     * @throws FileSystemException
     */
    public Ext2FileSystem(Device device, boolean readOnly)
            throws FileSystemException {
        super(device, readOnly);
        log.setLevel(Level.DEBUG);

        blockCache = new BlockCache(50, (float) 0.75);
        inodeCache = new INodeCache(50, (float) 0.75);

        //groupDescriptorLock = new Object();
        //superblockLock = new Object();
    }

    public void read() throws FileSystemException {
        ByteBuffer data;

        try {
            data = ByteBuffer.allocate(Superblock.SUPERBLOCK_LENGTH);

            //skip the first 1024 bytes (bootsector) and read the superblock
            //TODO: the superblock should read itself
            getApi().read(1024, data);
            //superblock = new Superblock(data, this);
            superblock = new Superblock();
            superblock.read(data.array(), this);

            //read the group descriptors
            groupCount = (int)Ext2Utils.ceilDiv(superblock.getBlocksCount(), superblock.getBlocksPerGroup());
            groupDescriptors = new GroupDescriptor[groupCount];
            iNodeTables = new INodeTable[groupCount];

            for (int i = 0; i < groupCount; i++) {
                //groupDescriptors[i]=new GroupDescriptor(i, this);
                groupDescriptors[i] = new GroupDescriptor();
                groupDescriptors[i].read(i, this);

                iNodeTables[i] = new INodeTable(this, (int) groupDescriptors[i]
                        .getInodeTable());
            }

        } catch (FileSystemException e) {
            throw e;
        } catch (Exception e) {
            throw new FileSystemException(e);
        }

        //check for unsupported filesystem options
        //(an unsupported INCOMPAT feature means that the fs may not be mounted
        // at all)
        if (hasIncompatFeature(Ext2Constants.EXT2_FEATURE_INCOMPAT_COMPRESSION))
            throw new FileSystemException(
                    getDevice().getId()
                            + " Unsupported filesystem feature (COMPRESSION) disallows mounting");
        if (hasIncompatFeature(Ext2Constants.EXT2_FEATURE_INCOMPAT_META_BG))
            throw new FileSystemException(
                    getDevice().getId()
                            + " Unsupported filesystem feature (META_BG) disallows mounting");
        if (hasIncompatFeature(Ext2Constants.EXT3_FEATURE_INCOMPAT_JOURNAL_DEV))
            throw new FileSystemException(
                    getDevice().getId()
                            + " Unsupported filesystem feature (JOURNAL_DEV) disallows mounting");
        if (hasIncompatFeature(Ext2Constants.EXT3_FEATURE_INCOMPAT_RECOVER))
            throw new FileSystemException(
                    getDevice().getId()
                            + " Unsupported filesystem feature (RECOVER) disallows mounting");

        //an unsupported RO_COMPAT feature means that the filesystem can only
        // be mounted readonly
        if (hasROFeature(Ext2Constants.EXT2_FEATURE_RO_COMPAT_LARGE_FILE)) {
            log
                    .info(getDevice().getId()
                            + " Unsupported filesystem feature (LARGE_FILE) forces readonly mode");
            setReadOnly(true);
        }
        if (hasROFeature(Ext2Constants.EXT2_FEATURE_RO_COMPAT_BTREE_DIR)) {
            log
                    .info(getDevice().getId()
                            + " Unsupported filesystem feature (BTREE_DIR) forces readonly mode");
            setReadOnly(true);
        }

        //if the filesystem has not been cleanly unmounted, mount it readonly
        if (superblock.getState() == Ext2Constants.EXT2_ERROR_FS) {
            log
                    .info(getDevice().getId()
                            + " Filesystem has not been cleanly unmounted, mounting it readonly");
            setReadOnly(true);
        }

        //if the filesystem has been mounted R/W, set it to "unclean"
        if (!isReadOnly()) {
            log.info(getDevice().getId() + " mounting fs r/w");
            superblock.setState(Ext2Constants.EXT2_ERROR_FS);
        }

        //log.info( "Ext2fs filesystem constructed sucessfully");
        log.debug("	superblock:	#blocks:		" + superblock.getBlocksCount()
                + "\n" + "				#blocks/group:	" + superblock.getBlocksPerGroup()
                + "\n" + "				#block groups:	" + groupCount + "\n"
                + "				block size:		" + superblock.getBlockSize() + "\n"
                + "				#inodes:		" + superblock.getINodesCount() + "\n"
                + "				#inodes/group:	" + superblock.getINodesPerGroup());        
    }
    
    public void create(int blockSize) throws FileSystemException {
        try {
            //create the superblock
            superblock = new Superblock();
            superblock.create(blockSize, this);

            //create the group descriptors
            groupCount = (int) Ext2Utils.ceilDiv(superblock.getBlocksCount(), superblock.getBlocksPerGroup());
            groupDescriptors = new GroupDescriptor[groupCount];

            iNodeTables = new INodeTable[groupCount];

            for (int i = 0; i < groupCount; i++) {
                groupDescriptors[i] = new GroupDescriptor();
                groupDescriptors[i].create(i, this);
            }

            //create each block group:
            //	create the block bitmap
            //	create the inode bitmap
            //	fill the inode table with zeroes
            for (int i = 0; i < groupCount; i++) {
                log.debug("creating group " + i);

                byte[] blockBitmap = new byte[blockSize];
                byte[] inodeBitmap = new byte[blockSize];

                //update the block bitmap: mark the metadata blocks allocated
                long iNodeTableBlock = groupDescriptors[i].getInodeTable();
                long firstNonMetadataBlock = iNodeTableBlock
                        + INodeTable.getSizeInBlocks(this);
                int metadataLength = (int) (firstNonMetadataBlock - (superblock
                        .getFirstDataBlock() + i
                        * superblock.getBlocksPerGroup()));
                for (int j = 0; j < metadataLength; j++)
                    BlockBitmap.setBit(blockBitmap, j);

                //set the padding at the end of the last block group
                if (i == groupCount - 1) {
                    for (long k = superblock.getBlocksCount(); k < groupCount
                            * superblock.getBlocksPerGroup(); k++)
                        BlockBitmap.setBit(blockBitmap, (int) (k % superblock
                                .getBlocksPerGroup()));
                }

                //update the inode bitmap: mark the special inodes allocated in
                // the first block group
                if (i == 0)
                    for (int j = 0; j < superblock.getFirstInode() - 1; j++)
                        INodeBitmap.setBit(inodeBitmap, j);

                //create an empty inode table
                byte[] emptyBlock = new byte[blockSize];
                for (long j = iNodeTableBlock; j < firstNonMetadataBlock; j++)
                    writeBlock(j, emptyBlock, false);

                iNodeTables[i] = new INodeTable(this, (int) iNodeTableBlock);

                writeBlock(groupDescriptors[i].getBlockBitmap(), blockBitmap,
                        false);
                writeBlock(groupDescriptors[i].getInodeBitmap(), inodeBitmap,
                        false);
            }

            log.info("superblock.getBlockSize(): " + superblock.getBlockSize());

            buildRootEntry();

            //write everything to disk
            flush();

        } catch (IOException ioe) {
            throw new FileSystemException("Unable to create filesystem", ioe);
        }

    }

    /**
     * Flush all changed structures to the device.
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        log.info("Flushing the contents of the filesystem");
        //update the inodes
        synchronized (inodeCache) {
            try {
                log.debug("inodecache size: " + inodeCache.size());
                for (INode iNode : inodeCache.values()) {
                    iNode.flush();
                }
            } catch (FileSystemException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            }
        }

        //update the group descriptors and the superblock copies
        updateFS();

        //flush the blocks
        synchronized (blockCache) {
            for (Block block : blockCache.values()) {
                block.flush();
            }
        }

        log.info("Filesystem flushed");
    }

    protected void updateFS() throws IOException {
        //updating one group descriptor updates all its copies
        for (int i = 0; i < groupCount; i++)
            groupDescriptors[i].updateGroupDescriptor();
        superblock.update();
    }

    public void close() throws IOException {
        //mark the filesystem clean

        superblock.setState(Ext2Constants.EXT2_VALID_FS);

        super.close();
    }

    /**
     * @see org.jnode.fs.spi.AbstractFileSystem#createRootEntry()
     */
    public FSEntry createRootEntry() throws IOException {
        try {
            return new Ext2Entry(getINode(Ext2Constants.EXT2_ROOT_INO), "/",
                    Ext2Constants.EXT2_FT_DIR, this, null);
        } catch (FileSystemException ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Return the block size of the file system
     */
    public int getBlockSize() {
        return superblock.getBlockSize();
    }

    /**
     * Read a data block and put it in the cache if it is not yet cached,
     * otherwise get it from the cache.
     * 
     * Synchronized access to the blockCache is important as the bitmap
     * operations are synchronized to the blocks (actually, to Block.getData()),
     * so at any point in time it has to be sure that no two copies of the same
     * block are stored in the cache.
     * 
     * @return data block nr
     */
    protected byte[] getBlock(long nr) throws IOException {
        if (isClosed())
            throw new IOException("FS closed (fs instance: " + this + ")");
        //log.debug("blockCache size: "+blockCache.size());

        int blockSize = superblock.getBlockSize();
        Block result;

        Integer key = new Integer((int) (nr));
        synchronized (blockCache) {
            //check if the block has already been retrieved
            if (blockCache.containsKey(key)) {
                result = blockCache.get(key);
                return result.getData();
            }
        }

        //perform the time-consuming disk read outside of the synchronized
        // block
        //advantage:
        //      -the lock is held for a shorter time, so other blocks that are
        //       already in the cache can be returned immediately and
        //       do not have to wait for a long disk read
        //disadvantage:
        //      -a single block can be retrieved more than once. However,
        //		 the block will be put in the cache only once in the second
        //		 synchronized block
        ByteBuffer data = ByteBuffer.allocate(blockSize);
        log.debug("Reading block " + nr + " (offset: " + nr * blockSize
                + ") from disk");
        getApi().read(nr * blockSize, data);

        //synchronize again
        synchronized (blockCache) {
            //check if the block has already been retrieved
            if (!blockCache.containsKey(key)) {
                result = new Block(this, nr, data.array());
                blockCache.put(key, result);
                return result.getData();
            } else {
                //it is important to ALWAYS return the block that is in
                //the cache (it is used in synchronization)
                result = blockCache.get(key);
                return result.getData();
            }
        }
    }

    /**
     * Update the block in cache, or write the block to disk
     * 
     * @param nr:
     *            block number
     * @param data:
     *            block data
     * @param forceWrite:
     *            if forceWrite is false, the block is only updated in the cache
     *            (if it was in the cache). If forceWrite is true, or the block
     *            is not in the cache, write it to disk.
     * @throws IOException
     */
    public void writeBlock(long nr, byte[] data, boolean forceWrite)
            throws IOException {
        if (isClosed())
            throw new IOException("FS closed");

        if (isReadOnly())
            throw new ReadOnlyFileSystemException(
                    "Filesystem is mounted read-only!");

        Block block;

        Integer key = new Integer((int) nr);
        int blockSize = superblock.getBlockSize();
        //check if the block is in the cache
        synchronized (blockCache) {
            if (blockCache.containsKey(key)) {
                block = blockCache.get(key);
                //update the data in the cache
                block.setData(data);
                if (forceWrite || SYNC_WRITE) {
                    //write the block to disk
                    ByteBuffer dataBuf = ByteBuffer.wrap(data, 0, blockSize);
                    getApi().write(nr * blockSize, dataBuf);
                    //timedWrite(nr, data);
                    block.setDirty(false);

                    log.debug("writing block " + nr + " to disk");
                } else
                    block.setDirty(true);
            } else {
                //If the block was not in the cache, I see no reason to put it
                //in the cache when it is written.
                //It is simply written to disk.
                ByteBuffer dataBuf = ByteBuffer.wrap(data, 0, blockSize);
                getApi().write(nr * blockSize, dataBuf);
                //timedWrite(nr, data);
            }
        }
    }

    /**
     * Helper class for timedWrite
     * 
     * @author blind
     */
    /*
     * class TimeoutWatcher extends TimerTask { Thread mainThread; public
     * TimeoutWatcher(Thread mainThread) { this.mainThread = mainThread; }
     * public void run() { mainThread.interrupt(); } }
     * 
     * private static final long TIMEOUT = 100;
     */
    /*
     * timedWrite writes to disk and waits for timeout, if the operation does
     * not finish in time, restart it. DO NOT CALL THIS DIRECTLY! ONLY TO BE
     * CALLED FROM writeBlock()! @param nr the number of the block to write
     * @param data the data in the block
     */
    /*
     * private void timedWrite(long nr, byte[] data) throws IOException{ boolean
     * finished = false; Timer writeTimer; while(!finished) { finished = true;
     * writeTimer = new Timer(); writeTimer.schedule(new
     * TimeoutWatcher(Thread.currentThread()), TIMEOUT); try{
     * getApi().write(nr*getBlockSize(), data, 0, (int)getBlockSize());
     * writeTimer.cancel(); }catch(IOException ioe) { //IDEDiskDriver will throw
     * an IOException with a cause of an InterruptedException //it the write is
     * interrupted if(ioe.getCause() instanceof InterruptedException) {
     * writeTimer.cancel(); log.debug("IDE driver interrupted during write
     * operation: probably timeout"); finished = false; } } } }
     * 
     * private void timedRead(long nr, byte[] data) throws IOException{ boolean
     * finished = false; Timer readTimer; while(!finished) { finished = true;
     * readTimer = new Timer(); readTimer.schedule(new
     * TimeoutWatcher(Thread.currentThread()), TIMEOUT); try{ getApi().read(
     * nr*getBlockSize(), data, 0, (int)getBlockSize()); readTimer.cancel();
     * }catch(IOException ioe) { //IDEDiskDriver will throw an IOException with
     * a cause of an InterruptedException //it the write is interrupted
     * if(ioe.getCause() instanceof InterruptedException) { readTimer.cancel();
     * log.debug("IDE driver interrupted during read operation: probably
     * timeout"); finished = false; } } } }
     */

    public Superblock getSuperblock() {
        return superblock;
    }

    /**
     * Return the inode numbered inodeNr (the first inode is #1)
     * 
     * Synchronized access to the inodeCache is important as the file/directory
     * operations are synchronized to the inodes, so at any point in time it has
     * to be sure that only one instance of any inode is present in the
     * filesystem.
     */
    public INode getINode(int iNodeNr) throws IOException, FileSystemException {
        if ((iNodeNr < 1) || (iNodeNr > superblock.getINodesCount()))
            throw new FileSystemException("INode number (" + iNodeNr
                    + ") out of range (0-" + superblock.getINodesCount() + ")");

        Integer key = new Integer(iNodeNr);

        log.debug("iNodeCache size: " + inodeCache.size());

        synchronized (inodeCache) {
            //check if the inode is already in the cache
            if (inodeCache.containsKey(key))
                return inodeCache.get(key);
        }

        //move the time consuming disk read out of the synchronized block
        //(see comments at getBlock())

        int group = (int) ((iNodeNr - 1) / superblock.getINodesPerGroup());
        int index = (int) ((iNodeNr - 1) % superblock.getINodesPerGroup());

        //get the part of the inode table that contains the inode
        INodeTable iNodeTable = iNodeTables[group];
        INode result = new INode(this, new INodeDescriptor(iNodeTable, iNodeNr,
                group, index));
        result.read(iNodeTable.getInodeData(index));

        synchronized (inodeCache) {
            //check if the inode is still not in the cache
            if (!inodeCache.containsKey(key)) {
                inodeCache.put(key, result);
                return result;
            } else
                return inodeCache.get(key);
        }
    }

    /**
     * Checks whether block <code>blockNr</code> is free, and if it is, then
     * allocates it with preallocation.
     * 
     * @param blockNr
     * @return @throws
     *         IOException
     */
    public BlockReservation testAndSetBlock(long blockNr) throws IOException {

        if (blockNr < superblock.getFirstDataBlock()
                || blockNr >= superblock.getBlocksCount())
            return new BlockReservation(false, -1, -1);
        int group = translateToGroup(blockNr);
        int index = translateToIndex(blockNr);

        /*
         * Return false if the block is not a data block but a filesystem
         * metadata block, as the beginning of each block group is filesystem
         * metadata: superblock copy (if present) block bitmap inode bitmap
         * inode table Free blocks begin after the inode table.
         */
        long iNodeTableBlock = groupDescriptors[group].getInodeTable();
        long firstNonMetadataBlock = iNodeTableBlock
                + INodeTable.getSizeInBlocks(this);

        if (blockNr < firstNonMetadataBlock)
            return new BlockReservation(false, -1, -1);

        //synchronize to the blockCache to avoid flushing the block between
        // reading it
        //and synchronizing to it
        synchronized (blockCache) {
            byte[] bitmap = getBlock(groupDescriptors[group].getBlockBitmap());
            synchronized (bitmap) {
                BlockReservation result = BlockBitmap.testAndSetBlock(bitmap,
                        index);
                //update the block bitmap
                if (result.isSuccessful()) {
                    writeBlock(groupDescriptors[group].getBlockBitmap(),
                            bitmap, false);
                    modifyFreeBlocksCount(group, -1 - result.getPreallocCount());
                    //result.setBlock(
                    // result.getBlock()+superblock.getFirstDataBlock() );
                    result.setBlock(blockNr);
                }
                return result;
            }
        }

    }

    /**
     * Create a new INode
     * 
     * @param preferredBlockBroup:
     *            first try to allocate the inode in this block group
     * @return
     */
    protected INode createINode(int preferredBlockBroup, int fileFormat,
            int accessRights, int uid, int gid) throws FileSystemException,
            IOException {
        if (preferredBlockBroup >= superblock.getBlocksCount())
            throw new FileSystemException("Block group " + preferredBlockBroup
                    + " does not exist");

        int groupNr = preferredBlockBroup;
        //first check the preferred block group, if it has any free inodes
        INodeReservation res = findFreeINode(groupNr);

        //if no free inode has been found in the preferred block group, then
        // try the others
        if (!res.isSuccessful()) {
            for (groupNr = 0; groupNr < superblock.getBlockGroupNr(); groupNr++) {
                res = findFreeINode(groupNr);
                if (res.isSuccessful()) {
                    break;
                }
            }
        }

        if (!res.isSuccessful())
            throw new FileSystemException("No free inodes found!");

        //a free inode has been found: create the inode and write it into the
        // inode table
        INodeTable iNodeTable = iNodeTables[preferredBlockBroup];
        //byte[] iNodeData = new byte[INode.INODE_LENGTH];
        int iNodeNr = res.getINodeNr((int) superblock.getINodesPerGroup());
        INode iNode = new INode(this, new INodeDescriptor(iNodeTable, iNodeNr,
                groupNr, res.getIndex()));
        iNode.create(fileFormat, accessRights, uid, gid);
        //trigger a write to disk
        iNode.update();

        log
                .debug("** NEW INODE ALLOCATED: inode number: "
                        + iNode.getINodeNr());

        //put the inode into the cache
        synchronized (inodeCache) {
            Integer key = new Integer(iNodeNr);
            if (inodeCache.containsKey(key))
                throw new FileSystemException(
                        "Newly allocated inode is already in the inode cache!?");
            else
                inodeCache.put(key, iNode);
        }

        return iNode;
    }

    /**
     * Find a free INode in the inode bitmap and allocate it
     * 
     * @param blockGroup
     * @return @throws
     *         IOException
     */
    protected INodeReservation findFreeINode(int blockGroup) throws IOException {
        GroupDescriptor gdesc = groupDescriptors[blockGroup];
        if (gdesc.getFreeInodesCount() > 0) {
            //synchronize to the blockCache to avoid flushing the block between
            // reading it
            //and synchronizing to it
            synchronized (blockCache) {
                byte[] bitmap = getBlock(gdesc.getInodeBitmap());

                synchronized (bitmap) {
                    INodeReservation result = INodeBitmap.findFreeINode(bitmap);

                    if (result.isSuccessful()) {
                        //update the inode bitmap
                        writeBlock(gdesc.getInodeBitmap(), bitmap, true);
                        modifyFreeInodesCount(blockGroup, -1);

                        result.setGroup(blockGroup);

                        return result;
                    }
                }
            }
        }
        return new INodeReservation(false, -1);
    }

    protected int translateToGroup(long i) {
        return (int) ((i - superblock.getFirstDataBlock()) / superblock
                .getBlocksPerGroup());
    }

    protected int translateToIndex(long i) {
        return (int) ((i - superblock.getFirstDataBlock()) % superblock
                .getBlocksPerGroup());
    }

    /**
     * Modify the number of free blocks in the block group
     * 
     * @param group
     * @param diff
     *            can be positive or negative
     */
    protected void modifyFreeBlocksCount(int group, int diff) {
        GroupDescriptor gdesc = groupDescriptors[group];
        gdesc.setFreeBlocksCount(gdesc.getFreeBlocksCount() + diff);

        superblock.setFreeBlocksCount(superblock.getFreeBlocksCount() + diff);
    }

    /**
     * Modify the number of free inodes in the block group
     * 
     * @param group
     * @param diff
     *            can be positive or negative
     */
    protected void modifyFreeInodesCount(int group, int diff) {
        GroupDescriptor gdesc = groupDescriptors[group];
        gdesc.setFreeInodesCount(gdesc.getFreeInodesCount() + diff);

        superblock.setFreeInodesCount(superblock.getFreeInodesCount() + diff);
    }

    /**
     * Modify the number of used directories in a block group
     * 
     * @param group
     * @param diff
     */
    protected void modifyUsedDirsCount(int group, int diff) {
        GroupDescriptor gdesc = groupDescriptors[group];
        gdesc.setUsedDirsCount(gdesc.getUsedDirsCount() + diff);
    }

    /**
     * Free up a block in the block bitmap.
     * 
     * @param blockNr
     * @throws FileSystemException
     * @throws IOException
     */
    public void freeBlock(long blockNr) throws FileSystemException, IOException {
        if (blockNr < 0 || blockNr >= superblock.getBlocksCount())
            throw new FileSystemException("Attempt to free nonexisting block ("
                    + blockNr + ")");

        int group = translateToGroup(blockNr);
        int index = translateToIndex(blockNr);
        GroupDescriptor gdesc = groupDescriptors[group];

        /*
         * Throw an exception if an attempt is made to free up a filesystem
         * metadata block (the beginning of each block group is filesystem
         * metadata): superblock copy (if present) block bitmap inode bitmap
         * inode table Free blocks begin after the inode table.
         */
        long iNodeTableBlock = groupDescriptors[group].getInodeTable();
        long firstNonMetadataBlock = iNodeTableBlock
                + INodeTable.getSizeInBlocks(this);

        if (blockNr < firstNonMetadataBlock)
            throw new FileSystemException(
                    "Attempt to free a filesystem metadata block!");

        //synchronize to the blockCache to avoid flushing the block between
        // reading it
        //and synchronizing to it
        synchronized (blockCache) {
            byte[] bitmap = getBlock(gdesc.getBlockBitmap());

            //at any time, only one copy of the Block exists in the cache, so
            // it is
            //safe to synchronize to the bitmapBlock object (it's part of
            // Block)
            synchronized (bitmap) {
                BlockBitmap.freeBit(bitmap, index);
                //update the bitmap block
                writeBlock(groupDescriptors[group].getBlockBitmap(), bitmap,
                        false);
                //gdesc.setFreeBlocksCount(gdesc.getFreeBlocksCount()+1);
                modifyFreeBlocksCount(group, 1);
            }
        }
    }

    /**
     * Find free blocks in the block group <code>group</code>'s block bitmap.
     * First check for a whole byte of free blocks (0x00) in the bitmap, then
     * check for any free bit. If blocks are found, mark them as allocated.
     * 
     * @return the index of the block (from the beginning of the partition)
     * @param group
     *            the block group to check
     * @param threshold
     *            find the free blocks only if there are at least
     *            <code>threshold</code> number of free blocks
     */
    public BlockReservation findFreeBlocks(int group, long threshold)
            throws IOException {
        GroupDescriptor gdesc = groupDescriptors[group];
        //see if it's worth to check the block group at all
        if (gdesc.getFreeBlocksCount() < threshold)
            return new BlockReservation(false, -1, -1, gdesc
                    .getFreeBlocksCount());

        /*
         * Return false if the block is not a data block but a filesystem
         * metadata block, as the beginning of each block group is filesystem
         * metadata: superblock copy (if present) block bitmap inode bitmap
         * inode table Free blocks begin after the inode table.
         */
        long iNodeTableBlock = groupDescriptors[group].getInodeTable();
        long firstNonMetadataBlock = iNodeTableBlock
                + INodeTable.getSizeInBlocks(this);
        int metadataLength = (int) (firstNonMetadataBlock - (superblock
                .getFirstDataBlock() + group * superblock.getBlocksPerGroup()));
        log.debug("group[" + group + "].getInodeTable()=" + iNodeTableBlock
                + ", iNodeTable.getSizeInBlocks()="
                + INodeTable.getSizeInBlocks(this));
        log.debug("metadata length for block group(" + group + "): "
                + metadataLength);

        BlockReservation result;

        //synchronize to the blockCache to avoid flushing the block between
        // reading it
        //and synchronizing to it
        synchronized (blockCache) {
            byte[] bitmapBlock = getBlock(gdesc.getBlockBitmap());

            //at any time, only one copy of the Block exists in the cache, so
            // it is
            //safe to synchronize to the bitmapBlock object (it's part of
            // Block)
            synchronized (bitmapBlock) {
                result = BlockBitmap
                        .findFreeBlocks(bitmapBlock, metadataLength);

                //if the reservation was successful, write the bitmap data to
                // disk
                //within the same synchronized block
                if (result.isSuccessful()) {
                    writeBlock(groupDescriptors[group].getBlockBitmap(),
                            bitmapBlock, true);
                    //gdesc.setFreeBlocksCount(gdesc.getFreeBlocksCount()-1-result.getPreallocCount());
                    modifyFreeBlocksCount(group, -1 - result.getPreallocCount());
                }
            }
        }

        if (result.isSuccessful()) {
            result.setBlock(group * getSuperblock().getBlocksPerGroup()
                    + superblock.getFirstDataBlock() + result.getBlock());
            result.setFreeBlocksCount(gdesc.getFreeBlocksCount());
        }

        return result;
    }

    /**
     * Returns the number of groups.
     * 
     * @return int
     */
    protected int getGroupCount() {
        return groupCount;
    }

    /**
     * Check whether the filesystem uses the given RO feature
     * (S_FEATURE_RO_COMPAT)
     * 
     * @param mask
     * @return
     */
    protected boolean hasROFeature(long mask) {
        return (mask & superblock.getFeatureROCompat()) != 0;
    }

    /**
     * Check whether the filesystem uses the given COMPAT feature
     * (S_FEATURE_INCOMPAT)
     * 
     * @param mask
     * @return
     */
    protected boolean hasIncompatFeature(long mask) {
        return (mask & superblock.getFeatureIncompat()) != 0;
    }

    /**
     * utility function for determining if a given block group has superblock
     * and group descriptor copies
     * 
     * @param a
     *            positive integer
     * @param b
     *            positive integer > 1
     * @return true if an n integer exists such that a=b^n; false otherwise
     */
    private boolean checkPow(int a, int b) {
        if (a <= 1)
            return true;
        while (true) {
            if (a == b)
                return true;
            if (a % b == 0) {
                a = a / b;
                continue;
            }
            return false;
        }
    }

    /**
     * With the sparse_super option set, a filesystem does not have a superblock
     * and group descriptor copy in every block group.
     * 
     * @param groupNr
     * @return true if the block group <code>groupNr</code> has a superblock
     *         and a group descriptor copy, otherwise false
     */
    protected boolean groupHasDescriptors(int groupNr) {
        if (hasROFeature(Ext2Constants.EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER))
            return (checkPow(groupNr, 3) || checkPow(groupNr, 5) || checkPow(
                    groupNr, 7));
        else
            return true;
    }

    /**
     *  
     */
    protected FSFile createFile(FSEntry entry) throws IOException {
        Ext2Entry e = (Ext2Entry) entry;
        return new Ext2File(e.getINode());
    }

    /**
     *  
     */
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        Ext2Entry e = (Ext2Entry) entry;
        return new Ext2Directory(e);
    }

    protected FSEntry buildRootEntry() throws IOException {
        //a free inode has been found: create the inode and write it into the
        // inode table
        INodeTable iNodeTable = iNodeTables[0];
        //byte[] iNodeData = new byte[INode.INODE_LENGTH];
        int iNodeNr = Ext2Constants.EXT2_ROOT_INO;
        INode iNode = new INode(this, new INodeDescriptor(iNodeTable, iNodeNr,
                0, iNodeNr - 1));
        int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU
                | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
        iNode.create(Ext2Constants.EXT2_S_IFDIR, rights, 0, 0);
        //trigger a write to disk
        iNode.update();

        //add the inode to the inode cache
        synchronized (inodeCache) {
            inodeCache.put(new Integer(Ext2Constants.EXT2_ROOT_INO), iNode);
        }

        modifyUsedDirsCount(0, 1);

        Ext2Entry rootEntry = new Ext2Entry(iNode, "/",
                Ext2Constants.EXT2_FT_DIR, this, null);
        ((Ext2Directory) rootEntry.getDirectory()).addINode(
                Ext2Constants.EXT2_ROOT_INO, ".", Ext2Constants.EXT2_FT_DIR);
        ((Ext2Directory) rootEntry.getDirectory()).addINode(
                Ext2Constants.EXT2_ROOT_INO, "..", Ext2Constants.EXT2_FT_DIR);
        rootEntry.getDirectory().addDirectory("lost+found");
        return rootEntry;
    }

    protected void handleFSError(Exception e) {
        //mark the fs as having errors
        superblock.setState(Ext2Constants.EXT2_ERROR_FS);
        if (superblock.getErrors() == Ext2Constants.EXT2_ERRORS_RO)
            setReadOnly(true); //remount readonly

        if (superblock.getErrors() == Ext2Constants.EXT2_ERRORS_PANIC)
            throw new RuntimeException("EXT2 FileSystem exception", e);
    }

    /**
     * @return Returns the blockCache (outside of this class only used to
     *         synchronize to)
     */
    protected synchronized BlockCache getBlockCache() {
        return blockCache;
    }

    /**
     * @return Returns the inodeCache (outside of this class only used to
     *         syncronized to)
     */
    protected synchronized INodeCache getInodeCache() {
        return inodeCache;
    }

	public long getFreeSpace() {
		// TODO implement me
		return 0;
	}

	public long getTotalSpace() {
		// TODO implement me 
		return 0;
	}

	public long getUsableSpace() {
		// TODO implement me 
		return 0;
	}
}
