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
 
package org.jnode.fs.ramfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * A Directory implementation in the system RAM
 * 
 * @author peda
 */
public class RAMDirectory implements FSEntry, FSDirectory {

    private RAMFileSystem filesystem;

    private RAMDirectory parent;
    private String directoryName;

    private long created;
    private long lastModified;
    private long lastAccessed;
    private FSAccessRights accessRights;

    /* if file is deleted, it is no longer valid */
    private boolean isValid = true;

    private HashMap<String, FSEntry> entries;

    /**
     * Constructor for a new RAMDirectory
     * 
     * @param fs
     * @param parent
     * @param name
     */
    public RAMDirectory(final RAMFileSystem fs, final RAMDirectory parent, final String name) {

        this.filesystem = fs;
        this.parent = parent;

        this.directoryName = name;
        this.created = this.lastModified = this.lastAccessed = System.currentTimeMillis();

        // TODO: accessRights

        entries = new HashMap<String, FSEntry>();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        return directoryName;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public FSDirectory getParent() {
        return parent;
    }

    public long getCreated() throws IOException {
        return created;
    }

    public long getLastModified() throws IOException {
        return lastModified;
    }

    public long getLastAccessed() throws IOException {
        return lastAccessed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        return false;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        return true;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#setName(java.lang.String)
     */
    public void setName(String newName) throws IOException {
        // TODO: check for special chars / normalize name
        directoryName = newName;
    }

    public void setCreated(long created) throws IOException {
        this.created = created;
    }

    public void setLastModified(long lastModified) throws IOException {
        this.lastModified = lastModified;
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        this.lastAccessed = lastAccessed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public FSFile getFile() throws IOException {
        throw new IOException("Not a file");
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getDirectory()
     */
    public FSDirectory getDirectory() throws IOException {
        return this;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getAccessRights()
     */
    public FSAccessRights getAccessRights() throws IOException {
        return accessRights;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isDirty()
     */
    public boolean isDirty() throws IOException {
        return false;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem getFileSystem() {
        return filesystem;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSDirectory#iterator()
     */
    public Iterator<? extends FSEntry> iterator() throws IOException {
        return entries.values().iterator();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
     */
    public FSEntry getEntry(String name) throws IOException {
        return entries.get(name);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
     */
    public FSEntry addFile(String name) throws IOException {
        RAMFile file = new RAMFile(this, name);
        entries.put(name, file);
        setLastModified(System.currentTimeMillis());
        return file;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
     */
    public FSEntry addDirectory(String name) throws IOException {
        RAMDirectory dir = new RAMDirectory(filesystem, this, name);
        entries.put(name, dir);
        setLastModified(System.currentTimeMillis());
        return dir;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
     */
    public void remove(String name) throws IOException {

        FSEntry entry = entries.remove(name);

        if (entry == null)
            throw new IOException("Entry not found");

        if (entry instanceof RAMFile) {
            RAMFile file = (RAMFile) entry;
            file.remove();
            setLastModified(System.currentTimeMillis());
        } else {
            RAMDirectory dir = (RAMDirectory) entry;
            dir.remove();
            setLastModified(System.currentTimeMillis());
        }
    }

    /**
     * removes the directory and all entries inside that directory
     * 
     * @throws IOException
     */
    private void remove() throws IOException {
        Iterator<FSEntry> itr = entries.values().iterator();
        while (itr.hasNext()) {
            FSEntry entry = itr.next();
            if (entry instanceof RAMFile) {
                RAMFile file = (RAMFile) entry;
                file.remove();
            } else {
                RAMDirectory dir = (RAMDirectory) entry;
                dir.remove();
            }
        }
        parent = null;
    }

    /**
     * (non-Javadoc)
     * @see org.jnode.fs.FSDirectory#flush()
     */
    public void flush() throws IOException {
        // nothing todo here
    }
}
