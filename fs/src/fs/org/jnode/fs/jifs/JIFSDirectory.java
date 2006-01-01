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
 * You should have received a copy of the GNU General Public License
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
import org.jnode.fs.jifs.directories.JIFSDplugins;
import org.jnode.fs.jifs.directories.JIFSDthreads;
import org.jnode.fs.jifs.files.JIFSFdevices;
import org.jnode.fs.jifs.files.JIFSFmemory;
import org.jnode.fs.jifs.files.JIFSFuptime;
import org.jnode.fs.jifs.files.JIFSFversion;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFSDirectory implements ExtFSEntry, FSDirectory {

    private boolean root = false;

    private String label;

    private FSDirectory parent;

    private final Map<String, FSEntry> entries;

    public JIFSDirectory() throws IOException {
        entries = new HashMap<String, FSEntry>();
    }
    
    public JIFSDirectory(String name) throws IOException {
    	this();
        label = name;
    }

    public JIFSDirectory(String name, FSDirectory parent) throws IOException {
        this(name);
        this.parent = parent;
    }

    public JIFSDirectory(String name, boolean root) throws IOException {
        this(name);
        this.root = root;
        JIFSDirectory dir;
        JIFSFile file;
        // file
        addFSE(new JIFSFuptime(this));
        addFSE(new JIFSFmemory(this));
        addFSE(new JIFSFversion(this));
        addFSE(new JIFSFdevices(this));
        // directory
        addFSE(new JIFSDthreads(this));
        addFSE(new JIFSDplugins(this));
        addFSE(new JIFSDirectory("extended",this));
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
    	//TODO -> extended FSEntry maybe have to be flushed
    }

    public FSEntry addDirectory(String name) {
        return null;
    }

    public FSEntry addFile(String name) {
        return null;
    }

    public Iterator<FSEntry> iterator() {
        return new JIFSDirIterator(entries.values());
    }

    public void remove(String name) throws IOException {
        throw new ReadOnlyFileSystemException("you can not remove from JNIFS..");
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

    public void setLastModified(long l) {
        return;
    }

    public long getLastModified() {
        return 0;
    }

    public void setName(String name) {
        this.label=name;
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
    
    public void setParent(FSDirectory parent){
		this.parent=parent;
	}

    public String getName() {
        return label;
    }

}
