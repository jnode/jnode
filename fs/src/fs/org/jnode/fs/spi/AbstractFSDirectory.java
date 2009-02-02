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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * An abstract implementation of FSDirectory that contains common things among
 * many FileSystems
 * 
 * @author Fabien DUMINY
 */
public abstract class AbstractFSDirectory extends AbstractFSObject implements FSDirectory {
    private static final Logger log = Logger.getLogger(AbstractFSDirectory.class);

    /* Table of entries */
    private FSEntryTable entries = FSEntryTable.EMPTY_TABLE;

    /* Is this directory a root-directory? */
    private boolean isRoot;

    /**
     * Constructor for a new non-root directory
     * 
     * @param fs
     */
    public AbstractFSDirectory(AbstractFileSystem fs) {
        this(fs, false);
    }

    /**
     * Constructor for a new directory (root or non-root)
     * 
     * @param fs
     * @param root true if it's a root directory
     */
    public AbstractFSDirectory(AbstractFileSystem fs, boolean root) {
        super(fs);
        this.isRoot = root;
    }

    /**
     * Print the contents of this directory to the given writer. Used for
     * debugging purposes.
     * 
     * @param out
     */
    public final void printTo(PrintWriter out) {
        checkEntriesLoaded();
        int freeCount = 0;
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            FSEntry entry = entries.get(i);
            if (entry != null) {
                out.println("0x" + Integer.toHexString(i) + " " + entry);
            } else {
                freeCount++;
            }
        }
        out.println("Unused entries " + freeCount);
    }

    /**
     * @see org.jnode.fs.FSDirectory#iterator()
     */
    public final Iterator<FSEntry> iterator() throws IOException {
        checkEntriesLoaded();
        return entries.iterator();
    }

    /**
     * Is this directory a root ?
     * 
     * @return if this directory is the root
     */
    public final boolean isRoot() {
        return isRoot;
    }

    /**
     * Gets the entry with the given name.
     * 
     * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
     */
    public final FSEntry getEntry(String name) throws IOException {
        // ensure entries are loaded from BlockDevice
        checkEntriesLoaded();

        return entries.get(name);
    }

    /**
     * Add a new directory with a given name
     * 
     * @param name
     * @return the new added directory
     * @throws IOException
     */
    public final synchronized FSEntry addDirectory(String name) throws IOException {
        log.debug("<<< BEGIN addDirectory " + name + " >>>");
        if (!canWrite())
            throw new ReadOnlyFileSystemException("Filesystem or directory is mounted read-only!");

        if (getEntry(name) != null) {
            throw new IOException("File or Directory already exists" + name);
        }
        FSEntry newEntry = createDirectoryEntry(name);
        setFreeEntry(newEntry);
        log.debug("<<< END addDirectory " + name + " >>>");
        return newEntry;
    }

    /**
     * Remove a file or directory with a given name
     * 
     * @param name
     * @throws IOException
     */
    public synchronized void remove(String name) throws IOException {
        if (!canWrite())
            throw new IOException("Filesystem or directory is mounted read-only!");
        if (entries.remove(name) >= 0) {
            setDirty();
            flush();
            return;
        } else
            throw new FileNotFoundException(name);
    }

    /**
     * Flush the contents of this directory to the persistent storage
     * 
     * @throws IOException
     */
    public final void flush() throws IOException {
        if (canWrite()) {
            boolean flushEntries = isEntriesLoaded() && entries.isDirty();
            if (isDirty() || flushEntries) {
                writeEntries(entries);
                entries.resetDirty();
                resetDirty();
            }
        }
    }

    /**
     * Read the entries of this directory from the persistent storage
     * 
     * @return a list of entries for this directory
     * @throws IOException
     */
    protected abstract FSEntryTable readEntries() throws IOException;

    /**
     * Write the entries of this directory to the persistent storage
     * 
     * @param entries a list of entries for this directory
     * @throws IOException
     */
    protected abstract void writeEntries(FSEntryTable entries) throws IOException;

    /**
     * Is this directory dirty (ie is there any data to save to device) ?
     * 
     * @return if this directory is dirty
     * @throws IOException
     */
    public final boolean isDirty() throws IOException {
        if (super.isDirty()) {
            return true;
        }
        // If entries are not loaded, they are clean (ie: in the storage) !
        if (isEntriesLoaded() && entries.isDirty())
            return true;
        return false;
    }

    /**
     * BE CAREFULL : don't call this method from the constructor of this class
     * because it call the method readEntries of the child classes that are not
     * yet initialized (constructed).
     */
    protected final void checkEntriesLoaded() {
        log.debug("<<< BEGIN checkEntriesLoaded >>>");
        if (!isEntriesLoaded()) {
            log.debug("checkEntriesLoaded : loading");
            try {
                if (canRead()) {
                    entries = readEntries();
                } else {
                    // the next time, we will call checkEntriesLoaded()
                    // we will retry to load entries
                    entries = FSEntryTable.EMPTY_TABLE;
                    log.debug("checkEntriesLoaded : can't read, using EMPTY_TABLE");
                }
                resetDirty();
            } catch (IOException e) {
                log.fatal("unable to read directory entries", e);
                // the next time, we will call checkEntriesLoaded()
                // we will retry to load entries
                entries = FSEntryTable.EMPTY_TABLE;
            }
        }
        log.debug("<<< END checkEntriesLoaded >>>");
    }

    /**
     * Have we already loaded our entries from device ?
     * 
     * @return if the entries are allready loaded from the device
     */
    private final boolean isEntriesLoaded() {
        return (entries != FSEntryTable.EMPTY_TABLE);
    }

    /**
     * Add a new file with a given name
     * 
     * @param name
     * @throws IOException
     * @return the added file entry
     */
    public final synchronized FSEntry addFile(String name) throws IOException {
        log.debug("<<< BEGIN addFile " + name + " >>>");
        if (!canWrite())
            throw new ReadOnlyFileSystemException("Filesystem or directory is mounted read-only!");

        if (getEntry(name) != null) {
            throw new IOException("File or directory already exists: " + name);
        }
        FSEntry newEntry = createFileEntry(name);
        setFreeEntry(newEntry);

        log.debug("<<< END addFile " + name + " >>>");
        return newEntry;
    }

    /**
     * Abstract method to create a new file entry from the given name
     * 
     * @param name
     * @return the new created file
     * @throws IOException
     */
    protected abstract FSEntry createFileEntry(String name) throws IOException;

    /**
     * Abstract method to create a new directory entry from the given name
     * @param name
     * @return the new created directory
     * @throws IOException
     */
    protected abstract FSEntry createDirectoryEntry(String name) throws IOException;

    /**
     * Find a free entry and set it with the given entry 
     * @param newEntry
     * @throws IOException
     */
    private final void setFreeEntry(FSEntry newEntry) throws IOException {
        checkEntriesLoaded();
        if (entries.setFreeEntry(newEntry) >= 0) {
            log.debug("setFreeEntry: free entry found !");
            // a free entry has been found
            setDirty();
            flush();
            return;
        }
    }

    /**
     * Return our entry table
     * @return the entry table
     */
    protected FSEntryTable getEntryTable() {
        return entries;
    }

    /**
     * @return a string representation of this instance.
     */
    public String toString() {
        return entries.toString();
    }
}
