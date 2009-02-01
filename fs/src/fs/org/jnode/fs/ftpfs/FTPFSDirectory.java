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
 
package org.jnode.fs.ftpfs;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.ReadOnlyFileSystemException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.enterprisedt.net.ftp.FTPFile;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSDirectory extends FTPFSEntry implements FSDirectory {
    private Map<String, FTPFSEntry> entries;

    FTPFSDirectory(FTPFileSystem fileSystem, FTPFile ftpFile) {
        super(fileSystem, ftpFile);
    }

    /**
     * Gets the entry with the given name.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry getEntry(String name) throws IOException {
        ensureEntries();
        return entries.get(name);
    }

    /**
     * Gets an iterator used to iterate over all the entries of this
     * directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    public Iterator<? extends FTPFSEntry> iterator() throws IOException {
        ensureEntries();
        return entries.values().iterator();
    }

    private void ensureEntries() throws IOException {
        try {
            if (entries == null) {
                entries = new HashMap<String, FTPFSEntry>();
                FTPFile[] ftpFiles = null;
                synchronized (fileSystem) {
                    ftpFiles = fileSystem.dirDetails(path());
                }
                for (FTPFile f : ftpFiles) {
                    FTPFSEntry e = f.isDir() ? new FTPFSDirectory(fileSystem, f) : new FTPFSFile(fileSystem, f);
                    e.setParent(this);
                    entries.put(f.getName(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Read error");
        }
    }

    String path() throws IOException {
        StringBuilder p = new StringBuilder("/");
        FTPFSDirectory root = fileSystem.getRootEntry();
        FTPFSDirectory d = this;
        while (d != root) {
            p.insert(0, d.getName());
            p.insert(0, '/');
            d = d.parent;
        }
        return p.toString();
    }

    /**
     * Add a new (sub-)directory with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry addDirectory(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    /**
     * Add a new file with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry addFile(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    /**
     * Save all dirty (unsaved) data to the device
     *
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        //nothing to do
    }

    /**
     * Remove the entry with the given name from this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public void remove(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }
}
