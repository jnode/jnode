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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.jarfs;

import java.io.IOException;
import java.util.jar.JarEntry;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ReadOnlyFileSystemException;

/** 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 */
public final class JarFSEntry implements FSEntry {

    private JarFSEntry parent;
    private String name;
    private JarEntry jarEntry;
    private JarFileSystem fs;

    public JarFSEntry(JarFileSystem fs, JarFSEntry parentEntry, JarEntry jarEntry, String name) {
        this.fs = fs;
        this.parent = parentEntry;
        this.jarEntry = jarEntry;
        this.name = name;
    }

    public JarEntry getJarEntry() {
        return jarEntry;
    }

    public JarFSEntry getParentFSEntry() {
        return parent;
    }

    /**
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        return name;
    }

    public FSDirectory getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getLastModified() throws IOException {
        return jarEntry.getTime();
    }

    public boolean isFile() {
        return !jarEntry.isDirectory();
    }

    public boolean isDirectory() {
        return jarEntry.isDirectory();
    }

    public void setName(String newName) throws IOException {
        throw new ReadOnlyFileSystemException("jar file system is readonly");
    }

    public void setLastModified(long lastModified) throws IOException {
        throw new ReadOnlyFileSystemException("jar file system is readonly");
    }

    public FSFile getFile() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FSDirectory getDirectory() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FSAccessRights getAccessRights() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDirty() throws IOException {
        return false;
    }

    public boolean isValid() {
        return true;
    }

    public FileSystem getFileSystem() {
        return fs;
    }
}
