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
 
package org.jnode.fs.spi;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * This class provide a basic implementation of {@link FileSystem} interface.
 * 
 * @author Fabien DUMINY
 */
public abstract class AbstractFileSystem<T extends FSEntry> implements FileSystem<T> {
	/** */
    private static final Logger log = Logger.getLogger(AbstractFileSystem.class);
    /** The device that contains the file system */
    private final Device device;
    /** API of the block device */
    private final BlockDeviceAPI api;
    /** Type of the file system */
    private final FileSystemType<? extends FileSystem<T>> type;
    /** Root enntry of the file system */
    private T rootEntry;
    /** The file system is read-only */
    private boolean readOnly;
    /** The file system is closed */
    private boolean closed;
    /** The cache of files */
    private HashMap<FSEntry, FSFile> files = new HashMap<FSEntry, FSFile>();
    /** The cache of directory */
    private HashMap<FSEntry, FSDirectory> directories = new HashMap<FSEntry, FSDirectory>();

    /**
     * Construct an AbstractFileSystem in specified readOnly mode
     * 
     * @param device device contains file system. This paramter is mandatory.
     * @param readOnly file system should be read-only.
     * 
     * @throws FileSystemException device is null or device has no {@link BlockDeviceAPI} defined.
     */
    public AbstractFileSystem(Device device, boolean readOnly,
            FileSystemType<? extends FileSystem<T>> type) throws FileSystemException {
        if (device == null)
            throw new FileSystemException("Device cannot be null.");

        this.device = device;

        try {
            api = device.getAPI(BlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Device is not a partition!", e);
        }

        this.closed = false;
        this.readOnly = readOnly;
        this.type = type;
    }

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * @see org.jnode.fs.FileSystem#getType()
     */
    public final FileSystemType<? extends FileSystem<T>> getType() {
        return type;
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public T getRootEntry() throws IOException {
        if (isClosed())
            throw new IOException("FileSystem is closed");

        if (rootEntry == null) {
            rootEntry = createRootEntry();
        }
        return rootEntry;
    }

    /**
     * @see org.jnode.fs.FileSystem#close()
     */
    public void close() throws IOException {
        if (!closed) {
            if (!readOnly) {
                flush();
            }
            api.flush();
            files.clear();
            directories.clear();
            rootEntry = null;
            files = null;
            directories = null;
            closed = true;
        }
    }

    /**
     * Save any cached data (files, directories, ...) to the device.
     * 
     * @throws IOException if error occurs during write of datas on the device.
     */
    public void flush() throws IOException {
        flushFiles();
        flushDirectories();
    }

    /**
     * Returns block device api.
     * 
     * @return {@link BlockDeviceAPI}.
     */
    public final BlockDeviceAPI getApi() {
        return api;
    }

    /**
     * Return file system block device api.
     * 
     * @return {@link BlockDeviceAPI}
     * 
     * @throws ApiNotFoundException if no api found for this file system device.
     */
    public final FSBlockDeviceAPI getFSApi() throws ApiNotFoundException {
        return device.getAPI(FSBlockDeviceAPI.class);
    }

    /*
     * (non-Javadoc)
     * @see org.jnode.fs.FileSystem#isClosed()
     */
    public final boolean isClosed() {
        return closed;
    }

    /**
     * Returns <tt>true</tt> if file system is read-only.
     * 
     * @return <tt>true</tt> if file system is read-only.
     */
    public final boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets file system as a read-only file system.
     * 
     * @param readOnly <tt>true</tt> if file system should be treated as a read-only file system.
     */
    protected final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Gets the file for the given entry.
     * 
     * @param entry
     * 
     * @return the {@link FSFile} associated with entry.
     * 
     * @throws IOException if file system is closed.
     */
    public final synchronized FSFile getFile(FSEntry entry) throws IOException {
        if (isClosed())
            throw new IOException("FileSystem is closed");

        FSFile file = files.get(entry);
        if (file == null) {
            file = createFile(entry);
            files.put(entry, file);
        }
        return file;
    }

    /**
     * Creates a new file from the entry
     * 
     * @param entry
     * @return a new {@link FSFile}
     * 
     * @throws IOException
     */
    protected abstract FSFile createFile(FSEntry entry) throws IOException;

    /**
     * Save all unsaved files from entry cache.
     * 
     * @throws IOException if error occurs during write of datas on the device.
     */
    private final void flushFiles() throws IOException {
        log.info("flushing files ...");
        for (FSFile f : files.values()) {
            if (log.isDebugEnabled()) {
                log.debug("flush: flushing file " + f);
            }
            f.flush();
        }
    }

    /**
     * Gets the file for the given entry.
     * 
     * @param entry
     * 
     * @return the {@link FSDirectory} associated with this entry
     * 
     * @throws IOException
     */
    public final synchronized FSDirectory getDirectory(FSEntry entry) throws IOException {
        if (isClosed())
            throw new IOException("FileSystem is closed");

        FSDirectory dir = directories.get(entry);
        if (dir == null) {
            dir = createDirectory(entry);
            directories.put(entry, dir);
        }
        return dir;
    }

    /**
     * Creates a new directory from the entry
     * 
     * @param entry
     * @return a new {@link FSDirectory}
     * 
     * @throws IOException
     */
    protected abstract FSDirectory createDirectory(FSEntry entry) throws IOException;

    /**
     * Save all unsaved files from entry cache.
     */
    private final void flushDirectories() {
        log.info("flushing directories ...");
        for (FSDirectory d : directories.values()) {
            if (log.isDebugEnabled()) {
                log.debug("flush: flushing directory " + d);
            }
            //TODO: uncomment this line
            //d.flush();
        }
    }

    /**
     * Creates a new root entry
     * 
     * @return {@link FSEntry} representing the new created root entry.
     * 
     * @throws IOException file system doesn't allow to create a new root entry.
     */
    protected abstract T createRootEntry() throws IOException;
}
