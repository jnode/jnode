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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.fs.FSFileSlackSpace;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.ext2.xattr.XAttrEntry;
import org.jnode.fs.spi.AbstractFSFile;
import org.jnode.fs.spi.AbstractFileSystem;
import org.jnode.util.ByteBufferUtils;

/**
 * @author Andras Nagy
 */
public class Ext2File extends AbstractFSFile implements FSFileSlackSpace {

    private final Ext2Entry entry;
    private final String name;
    private INode iNode;

    private static final Logger log = Logger.getLogger(Ext2File.class);

    public Ext2File(Ext2Entry entry) {
        super((AbstractFileSystem<?>) entry.getFileSystem());
        this.entry = entry;
        this.iNode = entry.getINode();
        this.name = entry.getName();
    }

    @Override
    public long getLength() {
        // log.debug("getLength(): "+iNode.getSize());
        return iNode.getSize();
    }

    @Override
    public void setLength(long length) throws IOException {
        if (!canWrite()) throw new ReadOnlyFileSystemException("FileSystem or File is readonly");

        long blockSize = iNode.getExt2FileSystem().getBlockSize();

        // synchronize to the inode cache to make sure that the inode does not
        // get
        // flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            // reread the inode before synchronizing to it to make sure
            // all threads use the same instance
            rereadInode();

            // lock the inode into the cache so it is not flushed before
            // synchronizing to it
            // (otherwise a new instance of INode referring to the same inode
            // could be put
            // in the cache resulting in the possibility of two threads
            // manipulating the same
            // inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }
        // a single inode may be represented by more than one Ext2Directory
        // instances,
        // but each will use the same instance of the underlying inode (see
        // Ext2FileSystem.getINode()),
        // so synchronize to the inode
        synchronized (iNode) {
            try {
                // if length<getLength(), then the file is truncated
                if (length < getLength()) {
                    long blockNr = length / blockSize;
                    long blockOffset = length % blockSize;
                    long nextBlock;
                    if (blockOffset == 0) nextBlock = blockNr;
                    else nextBlock = blockNr + 1;

                    for (long i = iNode.getAllocatedBlockCount() - 1; i >= nextBlock; i--) {
                        log.debug("setLength(): freeing up block " + i + " of inode");
                        iNode.freeDataBlock(i);
                    }
                    iNode.setSize(length);

                    iNode.setMtime(System.currentTimeMillis() / 1000);

                    return;
                }

                // if length>getLength(), then new blocks are allocated for the
                // file
                // The content of the new blocks is undefined (see the
                // setLength(long i)
                // method of java.io.RandomAccessFile
                if (length > getLength()) {
                    long len = length - getLength();
                    long blocksAllocated = getLengthInBlocks();
                    long bytesAllocated = getLength();
                    long bytesCovered = 0;
                    while (bytesCovered < len) {
                        long blockIndex = (bytesAllocated + bytesCovered) / blockSize;
                        long blockOffset = (bytesAllocated + bytesCovered) % blockSize;
                        long newSection = Math.min(len - bytesCovered, blockSize - blockOffset);

                        // allocate a new block if needed
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
                // setLength done, unlock the inode from the cache
                iNode.decLocked();
            }
        } // synchronized(inode)
    }

    @Override
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        if (fileOffset + destBuf.remaining() > getLength()) throw new IOException("Can't read past the file!");

        readImpl(fileOffset, destBuf);
    }

    /**
     * A read implementation that doesn't check the file length.
     *
     * @param fileOffset the offset to read from.
     * @param destBuf    the destination buffer.
     * @throws IOException if an error occurs reading.
     */
    public void readImpl(long fileOffset, ByteBuffer destBuf) throws IOException {
        final int len = destBuf.remaining();
        final int off = 0;
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();

        // synchronize to the inode cache to make sure that the inode does not
        // get flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            // reread the inode before synchronizing to it to make sure
            // all threads use the same instance
            rereadInode();

            // lock the inode into the cache so it is not flushed before
            // synchronizing to it
            // (otherwise a new instance of INode referring to the same inode
            // could be put
            // in the cache resulting in the possibility of two threads
            // manipulating the same
            // inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }

        if (log.isDebugEnabled()) {
            log.debug("File:" + name + " size:" + getLength() + " read offset: " + fileOffset + " len: "
                + dest.length);
        }

        // a single inode may be represented by more than one Ext2Directory
        // instances,
        // but each will use the same instance of the underlying inode (see
        // Ext2FileSystem.getINode()),
        // so synchronize to the inode
        synchronized (iNode) {
            try {
                if ((iNode.getMode() & Ext2Constants.EXT2_S_IFLNK) == Ext2Constants.EXT2_S_IFLNK) {
                    // Sym-links are a special case: the data seems to be stored inline in the iNode
                    System.arraycopy(iNode.getINodeBlockData(), 0, dest, 0, Math.min(64, dest.length));
                } else if ((iNode.getFlags() & Ext2Constants.EXT4_INLINE_DATA_FL) == Ext2Constants.EXT4_INLINE_DATA_FL) {
                    // Inline file data can be stored in both the inode i_block data, and also in a system.data
                    // attribute. If the inode is for a directory, then the data can only be in one or the other
                    XAttrEntry dataAttribute = iNode.getAttribute("system.data");
                    if (dataAttribute != null && dataAttribute.getValueSize() > 0) {
                        byte[] buffer;

                        if (entry.isDirectory()) {
                            log.debug("inline directory, length " + dataAttribute.getValueSize());
                            buffer = dataAttribute.getValue();
                        } else {
                            log.debug("inline file/directory in i_block/xattr, xattr len: " + dataAttribute.getValueSize());
                            ByteArrayOutputStream boas = new ByteArrayOutputStream();
                            boas.write(iNode.getINodeBlockData(), 0, Math.min(60, iNode.getINodeBlockData().length));
                            boas.write(dataAttribute.getValue());
                            buffer = boas.toByteArray();
                        }
                        System.arraycopy(buffer, 0, dest, 0, Math.min(buffer.length, dest.length));
                    } else {
                        log.debug("inline file/directory in i_block");
                        System.arraycopy(iNode.getINodeBlockData(), 0, dest, 0, Math.min(60, dest.length));
                    }
                } else {
                    long blockSize = iNode.getExt2FileSystem().getBlockSize();
                    long bytesRead = 0;
                    while (bytesRead < len) {
                        long blockNr = (fileOffset + bytesRead) / blockSize;
                        long blockOffset = (fileOffset + bytesRead) % blockSize;
                        long copyLength = Math.min(len - bytesRead, blockSize - blockOffset);

                        log.debug("blockNr: " + blockNr + ", blockOffset: " + blockOffset + ", copyLength: "
                            + copyLength + ", bytesRead: " + bytesRead);

                        System.arraycopy(iNode.getDataBlock(blockNr), (int) blockOffset, dest, off + (int) bytesRead,
                            (int) copyLength);

                        bytesRead += copyLength;
                    }
                }
            } catch (Throwable ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                // read done, unlock the inode from the cache
                iNode.decLocked();
            }
        }

        destBA.refreshByteBuffer();
    }

    @Override
    public void write(long fileOffset, ByteBuffer srcBuf) throws IOException {
        final int len = srcBuf.remaining();
        final int off = 0;
        // TODO optimize it also to use ByteBuffer at lower level
        final byte[] src = ByteBufferUtils.toArray(srcBuf);

        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("write in readonly filesystem");
        }

        // synchronize to the inode cache to make sure that the inode does not
        // get
        // flushed between reading it and locking it
        synchronized (((Ext2FileSystem) getFileSystem()).getInodeCache()) {
            // reread the inode before synchronizing to it to make sure
            // all threads use the same instance
            rereadInode();

            // lock the inode into the cache so it is not flushed before
            // synchronizing to it
            // (otherwise a new instance of INode referring to the same inode
            // could be put
            // in the cache resulting in the possibility of two threads
            // manipulating the same
            // inode at the same time because they would synchronize to
            // different INode instances)
            iNode.incLocked();
        }
        try {
            // a single inode may be represented by more than one Ext2File
            // instances,
            // but each will use the same instance of the underlying inode (see
            // Ext2FileSystem.getINode()),
            // so synchronize to the inode
            synchronized (iNode) {
                if (fileOffset > getLength()) throw new IOException(
                    "Can't write beyond the end of the file! (fileOffset: " + fileOffset + ", getLength()"
                        + getLength());
                if (off + len > src.length) throw new IOException("src is shorter than what you want to write");

                log.debug("write(fileOffset=" + fileOffset + ", src, off, len=" + len + ")");

                final long blockSize = iNode.getExt2FileSystem().getBlockSize();
                long blocksAllocated = iNode.getAllocatedBlockCount();
                long bytesWritten = 0;
                while (bytesWritten < len) {
                    long blockIndex = (fileOffset + bytesWritten) / blockSize;
                    long blockOffset = (fileOffset + bytesWritten) % blockSize;
                    long copyLength = Math.min(len - bytesWritten, blockSize - blockOffset);

                    // If only a part of the block is written, then read the
                    // block and update its contents with the data in src. If the
                    // whole block is overwritten, then skip reading it.
                    byte[] dest;
                    if (!((blockOffset == 0) && (copyLength == blockSize)) && (blockIndex < blocksAllocated))
                        dest = iNode.getDataBlock(blockIndex);
                    else dest = new byte[(int) blockSize];

                    System.arraycopy(src, (int) (off + bytesWritten), dest, (int) blockOffset, (int) copyLength);

                    // allocate a new block if needed
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

                    // write the block
                    iNode.writeDataBlock(blockIndex, dest);

                    bytesWritten += copyLength;
                }
                iNode.setSize(fileOffset + len);

                iNode.setMtime(System.currentTimeMillis() / 1000);
            }
        } catch (IOException ex) {
            // ... this avoids wrapping an IOException inside another one.
            throw ex;
        } catch (Throwable ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        } finally {
            // write done, unlock the inode from the cache
            iNode.decLocked();
        }
    }

    @Override
    public void flush() throws IOException {
        log.debug("Ext2File.flush()");
        iNode.update();
        // update the group descriptors and superblock: needed if blocks have
        // been allocated or deallocated
        iNode.getExt2FileSystem().updateFS();
    }

    private long getLengthInBlocks() {
        return iNode.getSizeInBlocks();
    }

    private void rereadInode() throws IOException {
        long iNodeNr = iNode.getINodeNr();
        try {
            iNode = ((Ext2FileSystem) getFileSystem()).getINode(iNodeNr);
        } catch (FileSystemException ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    @Override
    public byte[] getSlackSpace() throws IOException {
        int blockSize = ((Ext2FileSystem) getFileSystem()).getBlockSize();

        int slackSpaceSize = blockSize - (int) (getLength() % blockSize);

        if (slackSpaceSize == blockSize) {
            slackSpaceSize = 0;
        }

        byte[] slackSpace = new byte[slackSpaceSize];
        readImpl(getLength(), ByteBuffer.wrap(slackSpace));

        return slackSpace;
    }
}
