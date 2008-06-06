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
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.spi.AbstractFSFile;
import org.jnode.util.ByteBufferUtils;

/**
 * @author Andras Nagy
 */
public class Ext2File extends AbstractFSFile {

    INode iNode;

    private final Logger log = Logger.getLogger(getClass());

    public Ext2File(INode iNode) {
        super(iNode.getExt2FileSystem());
        this.iNode = iNode;
        log.setLevel(Level.DEBUG);
    }

    /**
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        //log.debug("getLength(): "+iNode.getSize());
        return iNode.getSize();
    }

    private long getLengthInBlocks() {
        return iNode.getSizeInBlocks();
    }

    private void rereadInode() throws IOException {
        int iNodeNr = iNode.getINodeNr();
        try {
            iNode = ((Ext2FileSystem) getFileSystem()).getINode(iNodeNr);
        } catch (FileSystemException ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {
        if (!canWrite())
            throw new ReadOnlyFileSystemException("FileSystem or File is readonly");

        long blockSize = iNode.getExt2FileSystem().getBlockSize();

        //synchronize to the inode cache to make sure that the inode does not
        // get
        //flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            //reread the inode before synchronizing to it to make sure
            //all threads use the same instance
            rereadInode();

            //lock the inode into the cache so it is not flushed before
            // synchronizing to it
            //(otherwise a new instance of INode referring to the same inode
            // could be put
            //in the cache resulting in the possibility of two threads
            // manipulating the same
            //inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }
        //a single inode may be represented by more than one Ext2Directory
        // instances,
        //but each will use the same instance of the underlying inode (see
        // Ext2FileSystem.getINode()),
        //so synchronize to the inode
        synchronized (iNode) {
            try {
                //if length<getLength(), then the file is truncated
                if (length < getLength()) {
                    long blockNr = length / blockSize;
                    long blockOffset = length % blockSize;
                    long nextBlock;
                    if (blockOffset == 0)
                        nextBlock = blockNr;
                    else
                        nextBlock = blockNr + 1;

                    for (long i = iNode.getAllocatedBlockCount() - 1; i >= nextBlock; i--) {
                        log.debug("setLength(): freeing up block " + i + " of inode");
                        iNode.freeDataBlock(i);
                    }
                    iNode.setSize(length);

                    iNode.setMtime(System.currentTimeMillis() / 1000);

                    return;
                }

                //if length>getLength(), then new blocks are allocated for the
                // file
                //The content of the new blocks is undefined (see the
                // setLength(long i)
                //method of java.io.RandomAccessFile
                if (length > getLength()) {
                    long len = length - getLength();
                    long blocksAllocated = getLengthInBlocks();
                    long bytesAllocated = getLength();
                    long bytesCovered = 0;
                    while (bytesCovered < len) {
                        long blockIndex = (bytesAllocated + bytesCovered) / blockSize;
                        long blockOffset = (bytesAllocated + bytesCovered) % blockSize;
                        long newSection = Math.min(len - bytesCovered, blockSize - blockOffset);

                        //allocate a new block if needed
                        if (blockIndex >= blocksAllocated) {
                            iNode.allocateDataBlock(blockIndex);
                            blocksAllocated++;
                        }

                        bytesCovered += newSection;
                    }
                    iNode.setSize(length);

                    iNode.setMtime(System.currentTimeMillis() / 1000);

                    return;
                }
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                //setLength done, unlock the inode from the cache
                iNode.decLocked();
            }
        } // synchronized(inode)
    }

    /**
     * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
     */
    //public void read(long fileOffset, byte[] dest, int off, int len)
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        final int len = destBuf.remaining();
        final int off = 0;
        //TODO optimize it also to use ByteBuffer at lower level 
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();

        //synchronize to the inode cache to make sure that the inode does not
        // get
        //flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            //reread the inode before synchronizing to it to make sure
            //all threads use the same instance
            rereadInode();

            //lock the inode into the cache so it is not flushed before
            // synchronizing to it
            //(otherwise a new instance of INode referring to the same inode
            // could be put
            //in the cache resulting in the possibility of two threads
            // manipulating the same
            //inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }
        //a single inode may be represented by more than one Ext2Directory
        // instances,
        //but each will use the same instance of the underlying inode (see
        // Ext2FileSystem.getINode()),
        //so synchronize to the inode
        synchronized (iNode) {
            try {
                if (len + off > getLength())
                    throw new IOException("Can't read past the file!");
                long blockSize = iNode.getExt2FileSystem().getBlockSize();
                long bytesRead = 0;
                while (bytesRead < len) {
                    long blockNr = (fileOffset + bytesRead) / blockSize;
                    long blockOffset = (fileOffset + bytesRead) % blockSize;
                    long copyLength = Math.min(len - bytesRead, blockSize - blockOffset);

                    log.debug("blockNr: " + blockNr + ", blockOffset: " + blockOffset + ", copyLength: " + copyLength +
                            ", bytesRead: " + bytesRead);

                    System.arraycopy(iNode.getDataBlock(blockNr), (int) blockOffset, dest, off + (int) bytesRead,
                            (int) copyLength);

                    bytesRead += copyLength;
                }
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                //read done, unlock the inode from the cache
                iNode.decLocked();
            }
        }

        destBA.refreshByteBuffer();
    }

    /**
     * Write into the file. fileOffset is between 0 and getLength() (see the
     * methods write(byte[], int, int), setPosition(long), setLength(long) in
     * org.jnode.fs.service.def.FileHandleImpl)
     * 
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    //public void write(long fileOffset, byte[] src, int off, int len)
    public void write(long fileOffset, ByteBuffer srcBuf) throws IOException {
        final int len = srcBuf.remaining();
        final int off = 0;
        //TODO optimize it also to use ByteBuffer at lower level                 
        final byte[] src = ByteBufferUtils.toArray(srcBuf);

        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("write in readonly filesystem");
        }

        //synchronize to the inode cache to make sure that the inode does not
        // get
        //flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            //reread the inode before synchronizing to it to make sure
            //all threads use the same instance
            rereadInode();

            //lock the inode into the cache so it is not flushed before
            // synchronizing to it
            //(otherwise a new instance of INode referring to the same inode
            // could be put
            //in the cache resulting in the possibility of two threads
            // manipulating the same
            //inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }
        try {
            //a single inode may be represented by more than one Ext2File
            // instances,
            //but each will use the same instance of the underlying inode (see
            // Ext2FileSystem.getINode()),
            //so synchronize to the inode
            synchronized (iNode) {
                if (fileOffset > getLength())
                    throw new IOException("Can't write beyond the end of the file! (fileOffset: " + fileOffset +
                            ", getLength()" + getLength());
                if (off + len > src.length)
                    throw new IOException("src is shorter than what you want to write");

                log.debug("write(fileOffset=" + fileOffset + ", src, off, len=" + len + ")");

                final long blockSize = iNode.getExt2FileSystem().getBlockSize();
                long blocksAllocated = iNode.getAllocatedBlockCount();
                long bytesWritten = 0;
                while (bytesWritten < len) {
                    long blockIndex = (fileOffset + bytesWritten) / blockSize;
                    long blockOffset = (fileOffset + bytesWritten) % blockSize;
                    long copyLength = Math.min(len - bytesWritten, blockSize - blockOffset);

                    //If only a part of the block is written, then read the
                    // block
                    //and update its contents with the data in src. If the
                    // whole block
                    //is overwritten, then skip reading it.
                    byte[] dest;
                    if (!((blockOffset == 0) && (copyLength == blockSize)) && (blockIndex < blocksAllocated))
                        dest = iNode.getDataBlock(blockIndex);
                    else
                        dest = new byte[(int) blockSize];

                    System.arraycopy(src, (int) (off + bytesWritten), dest, (int) blockOffset, (int) copyLength);

                    //allocate a new block if needed
                    if (blockIndex >= blocksAllocated) {
                        try {
                            iNode.allocateDataBlock(blockIndex);
                        } catch (FileSystemException ex) {
                            final IOException ioe = new IOException("Internal filesystem exception");
                            ioe.initCause(ex);
                            throw ioe;
                        }
                        blocksAllocated++;
                    }

                    //write the block
                    iNode.writeDataBlock(blockIndex, dest);

                    bytesWritten += copyLength;
                }
                iNode.setSize(fileOffset + len);

                iNode.setMtime(System.currentTimeMillis() / 1000);
            }
        } catch (Throwable ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        } finally {
            //write done, unlock the inode from the cache
            iNode.decLocked();
        }
    }

    /**
     * Flush any cached data to the disk.
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        log.debug("Ext2File.flush()");
        iNode.update();
        //update the group descriptors and superblock: needed if blocks have
        // been
        //allocated or deallocated
        iNode.getExt2FileSystem().updateFS();
    }
}
