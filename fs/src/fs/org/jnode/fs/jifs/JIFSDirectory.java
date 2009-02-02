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
 
package org.jnode.fs.jifs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFSDirectory implements ExtFSEntry, FSDirectory {

    private String label;

    private FSDirectory parent;

    private final Map<String, FSEntry> entries;

    public JIFSDirectory() {
        entries = new HashMap<String, FSEntry>();
    }

    public JIFSDirectory(String name) {
        this();
        label = name;
    }

    public JIFSDirectory(String name, FSDirectory parent) {
        this(name);
        this.parent = parent;
    }

    public void refresh() {
        return;
    }

    protected void clear() {
        entries.clear();
    }

    public void addFSE(FSEntry entry) {
        entries.put(entry.getName(), entry);
    }

    /**
     * Flush the contents of this directory to the persistent storage
     */
    public void flush() throws IOException {
        // TODO -> extended FSEntry maybe have to be flushed
    }

    public FSEntry addDirectory(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    public FSEntry addFile(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    public Iterator<FSEntry> iterator() {
        return new JIFSDirIterator(entries.values());
    }

    public void remove(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    public boolean isValid() {
        return true;
    }

    public boolean isDirty() {
        return false;
    }

    public FileSystem getFileSystem() {
        // TODO
        return null;
    }

    public FSEntry getEntry(String name) {
        return entries.get(name);
    }

    public FSAccessRights getAccessRights() {
        return null;
    }

    public FSDirectory getDirectory() {
        return this;
    }

    public FSFile getFile() {
        return null;
    }

    public long getLastModified() {
        return 0;
    }

    public void setLastModified(long lastModified) {
    }

    public void setName(String name) {
        this.label = name;
    }

    public boolean isDirectory() {
        return true;
    }

    public boolean isFile() {
        return false;
    }

    public FSDirectory getParent() {
        return parent;
    }

    public void setParent(FSDirectory parent) {
        this.parent = parent;
    }

    public String getName() {
        return label;
    }

}
