/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.jarfs;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 * 
 */
public final class JarFSDirectory implements FSDirectory {

    private final JarFSEntry entry;
    private Map<String, JarFSEntry> jarEntries;

    /**
     * @param entry
     */
    public JarFSDirectory(JarFSEntry entry, Map<String, JarFSEntry> entries) {
        this.entry = entry;
        this.jarEntries = entries;
    }

    /**
     * @see org.jnode.fs.FSDirectory#iterator()
     */
    public Iterator<? extends FSEntry> iterator() {
        return jarEntries.values().iterator();
    }

    /**
     * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
     */
    public FSEntry getEntry(String name) throws IOException {
        return jarEntries.get(name);
    }

    /**
     * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
     */
    public FSEntry addFile(String name) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are always readOnly");
    }

    /**
     * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
     */
    public FSEntry addDirectory(String name) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are always readOnly");
    }

    /**
     * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
     */
    public void remove(String name) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are always readOnly");
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public final boolean isValid() {
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public final FileSystem getFileSystem() {
        return entry.getFileSystem();
    }

    /**
     * Save all dirty (unsaved) data to the device
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        // jar file systems are always readOnly
    }
}
