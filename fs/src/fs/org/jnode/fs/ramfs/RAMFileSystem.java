/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.fs.ramfs;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * A Filesystem implementation in the system RAM.
 * 
 * @author peda
 */
public class RAMFileSystem implements FileSystem<RAMDirectory> {

    private Device device;
    private boolean readOnly;

    private RAMDirectory root;

    private final long maxSize;
    private long summedBufferSize;
    private long summedFileSize;
    private final RAMFileSystemType type;

    /**
     * Constructor for RAMFileSystem
     * 
     * @param device
     * @param readOnly
     * @param maxSize
     * @throws FileSystemException
     */
    public RAMFileSystem(Device device, boolean readOnly, long maxSize, RAMFileSystemType type)
        throws FileSystemException {
        this.type = type;
        this.device = device;
        this.readOnly = readOnly;

        this.maxSize = maxSize;
        summedBufferSize = 0;
        summedFileSize = 0;

        root = new RAMDirectory(this, null, "");
    }

    public final RAMFileSystemType getType() {
        return this.type;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public Device getDevice() {
        return device;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public RAMDirectory getRootEntry() throws IOException {
        if (isClosed())
            throw new IOException("Filesystem closed");
        return root;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#isReadOnly()
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#close()
     */
    public void close() throws IOException {
        root = null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#isClosed()
     */
    public boolean isClosed() {
        return (root == null);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getTotalSpace()
     */
    public long getTotalSpace() {
        return maxSize;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getFreeSpace()
     */
    public long getFreeSpace() {
        return maxSize - summedBufferSize;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getUsableSpace()
     */
    public long getUsableSpace() {
        return maxSize - summedFileSize;
    }

    synchronized void addSummmedBufferSize(long toAdd) {
        summedBufferSize += toAdd;
    }

    synchronized void addSummedFileSize(long toAdd) {
        summedFileSize += toAdd;
    }
}
