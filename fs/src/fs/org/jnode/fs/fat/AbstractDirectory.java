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
 
package org.jnode.fs.fat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * @author epr
 */
public abstract class AbstractDirectory extends FatObject implements FSDirectory {

    protected Vector<FatBasicDirEntry> entries = new Vector<FatBasicDirEntry>();
    private boolean _dirty;
    protected FatFile file;

    // for root
    protected AbstractDirectory(FatFileSystem fs, int nrEntries) {
        super(fs);
        entries.setSize(nrEntries);
        _dirty = false;
    }

    protected AbstractDirectory(FatFileSystem fs, int nrEntries, FatFile file) {
        this(fs, nrEntries);
        this.file = file;
    }

    protected AbstractDirectory(FatFileSystem fs, FatFile myFile) {
        this(fs, (int) myFile.getLength() / 32, myFile);
    }

    /**
     * Gets an iterator to iterate over all entries. The iterated objects are
     * all instance DirEntry.
     * 
     * @return Iterator
     */
    public Iterator<FSEntry> iterator() {
        return new DirIterator();
    }

    /**
     * Add a directory entry.
     * 
     * @param nameExt
     * @throws IOException
     */
    protected synchronized FatDirEntry addFatFile(String nameExt) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("addFile in readonly filesystem");
        }

        if (getFatEntry(nameExt) != null) {
            throw new IOException("File already exists" + nameExt);
        }
        final FatDirEntry newEntry = new FatDirEntry(this, splitName(nameExt), splitExt(nameExt));
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            FatBasicDirEntry e = entries.get(i);
            if (e == null) {
                entries.set(i, newEntry);
                setDirty();
                flush();
                return newEntry;
            }
        }
        int newSize = size + 512 / 32;
        if (canChangeSize(newSize)) {
            entries.ensureCapacity(newSize);
            setDirty();
            flush();
            return newEntry;
        }
        throw new IOException("Directory is full");
    }

    /**
     * Add a new file with a given name to this directory.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry addFile(String name) throws IOException {
        return addFatFile(name);
    }

    /**
     * Add a directory entry of the type directory.
     * 
     * @param nameExt
     * @param parentCluster
     * @throws IOException
     */
    protected synchronized FatDirEntry addFatDirectory(String nameExt, long parentCluster) throws IOException {
        final FatDirEntry entry = addFatFile(nameExt);
        final int clusterSize = getFatFileSystem().getClusterSize();
        entry.setFlags(FatConstants.F_DIRECTORY);
        final FatFile file = entry.getFatFile();
        file.setLength(clusterSize);

        //TODO optimize it also to use ByteBuffer at lower level        
        //final byte[] buf = new byte[clusterSize];
        final ByteBuffer buf = ByteBuffer.allocate(clusterSize);

        // Clean the contents of this cluster to avoid reading strange data
        // in the directory.
        //file.write(0, buf, 0, buf.length);
        file.write(0, buf);

        file.getDirectory().initialize(file.getStartCluster(), parentCluster);
        flush();
        return entry;
    }

    /**
     * Add a new (sub-)directory with a given name to this directory.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry addDirectory(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException("addDirectory in readonly filesystem");
        }

        final long parentCluster;
        if (file == null) {
            parentCluster = 0;
        } else {
            parentCluster = file.getStartCluster();
        }
        return addFatDirectory(name, parentCluster);
    }

    /**
     * Gets the number of directory entries in this directory
     * 
     * @return int
     */
    public int getSize() {
        return entries.size();
    }

    /**
     * Search for an entry with a given name.ext
     * 
     * @param nameExt
     * @return FatDirEntry null == not found
     */
    protected FatDirEntry getFatEntry(String nameExt) {

        final String name = splitName(nameExt);
        final String ext = splitExt(nameExt);
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            final FatBasicDirEntry entry = entries.get(i);
            if (entry != null && entry instanceof FatDirEntry) {
                FatDirEntry fde = (FatDirEntry) entry;
                if (name.equalsIgnoreCase(fde.getNameOnly()) && ext.equalsIgnoreCase(fde.getExt())) {
                    return fde;
                }
            }
        }
        return null;
    }

    /**
     * Gets the entry with the given name.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry getEntry(String name) throws IOException {
        final FatDirEntry entry = getFatEntry(name);
        if (entry == null) {
            throw new FileNotFoundException(name);
        } else {
            return entry;
        }
    }

    /**
     * Remove a file or directory with a given name
     * 
     * @param nameExt
     */
    public synchronized void remove(String nameExt) throws IOException {
        FatDirEntry entry = getFatEntry(nameExt);
        if (entry == null) {
            throw new FileNotFoundException(nameExt);
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == entry) {
                entries.set(i, null);
                setDirty();
                flush();
                return;
            }
        }
    }

    /**
     * Print the contents of this directory to the given writer. Used for
     * debugging purposes.
     * 
     * @param out
     */
    public void printTo(PrintWriter out) {
        int freeCount = 0;
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            FatBasicDirEntry entry = entries.get(i);
            if (entry != null) {
                out.println("0x" + Integer.toHexString(i) + " " + entries.get(i));
            } else {
                freeCount++;
            }
        }
        out.println("Unused entries " + freeCount);
    }

    class DirIterator implements Iterator<FSEntry> {

        private int offset = 0;

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            int size = entries.size();
            while (offset < size) {
                FatBasicDirEntry e = entries.get(offset);
                if ((e != null) && e instanceof FatDirEntry && !((FatDirEntry) e).isDeleted()) {
                    return true;
                } else {
                    offset++;
                }
            }
            return false;
        }

        /**
         * @see java.util.Iterator#next()
         */
        public FSEntry next() {
            int size = entries.size();
            while (offset < size) {
                FatBasicDirEntry e = entries.get(offset);
                if ((e != null) && (e instanceof FatDirEntry) && !((FatDirEntry) e).isDeleted()) {
                    offset++;
                    return (FSEntry) e;
                } else {
                    offset++;
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns the dirty.
     * 
     * @return boolean
     */
    public boolean isDirty() {
        if (_dirty) {
            return true;
        }
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            FatBasicDirEntry entry = entries.get(i);
            if ((entry != null) && (entry instanceof FatDirEntry)) {
                if (((FatDirEntry) entry).isDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Mark this directory as dirty.
     */
    protected final void setDirty() {
        this._dirty = true;
    }

    /**
     * Mark this directory as not dirty.
     */
    protected final void resetDirty() {
        this._dirty = false;
    }

    /**
     * Can this directory change size of <code>newSize</code> directory
     * entries?
     * 
     * @param newSize
     * @return boolean
     */
    protected abstract boolean canChangeSize(int newSize);

    protected String splitName(String nameExt) {
        int i = nameExt.indexOf('.');
        if (i < 0) {
            return nameExt;
        } else {
            return nameExt.substring(0, i);
        }
    }

    protected String splitExt(String nameExt) {
        int i = nameExt.indexOf('.');
        if (i < 0) {
            return "";
        } else {
            return nameExt.substring(i + 1);
        }
    }

    /**
     * Sets the first two entries '.' and '..' in the directory
     * 
     * @param parentCluster
     */
    protected void initialize(long myCluster, long parentCluster) {
        FatDirEntry e = new FatDirEntry(this, ".", "");
        entries.set(0, e);
        e.setFlags(FatConstants.F_DIRECTORY);
        e.setStartCluster((int) myCluster);
        e = new FatDirEntry(this, "..", "");
        entries.set(1, e);
        e.setFlags(FatConstants.F_DIRECTORY);
        e.setStartCluster((int) parentCluster);
    }

    /**
     * Flush the contents of this directory to the persistent storage
     */
    public abstract void flush() throws IOException;

    /**
     * Read the contents of this directory from the given byte array
     * 
     * @param src
     */
    protected synchronized void read(byte[] src) {
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            int index = i * 32;
            if (src[index] == 0) {
                entries.set(i, null);
            } else {
                FatBasicDirEntry entry = FatDirEntry.fatDirEntryFactory(this, src, index);
                entries.set(i, entry);
            }
        }
    }

    /**
     * Write the contents of this directory to the given device at the given
     * offset.
     * 
     * @param dest
     */
    protected synchronized void write(byte[] dest) {
        int size = entries.size();
        byte[] empty = new byte[32];
        for (int i = 0; i < size; i++) {
            FatBasicDirEntry entry = entries.get(i);
            if (entry != null) {
                entry.write(dest, i * 32);
            } else {
                System.arraycopy(empty, 0, dest, i * 32, 32);
            }
        }
    }

}
