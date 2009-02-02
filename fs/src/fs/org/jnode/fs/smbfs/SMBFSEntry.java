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
 
package org.jnode.fs.smbfs;

import java.io.IOException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSEntry;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class SMBFSEntry implements FSEntry {
    SmbFile smbFile;
    SMBFSDirectory parent;

    protected SMBFSEntry(SMBFSDirectory parent, SmbFile smbFile) {
        this.parent = parent;
        this.smbFile = smbFile;
    }

    /**
     * @see org.jnode.fs.FSEntry#getAccessRights()
     */
    public FSAccessRights getAccessRights() throws IOException {
        //todo implement it
        return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#getDirectory()
     */
    public SMBFSDirectory getDirectory() throws IOException {
        return (SMBFSDirectory) this;
    }

    /**
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public SMBFSFile getFile() throws IOException {
        return (SMBFSFile) this;
    }

    public long getCreated() throws IOException {
        return smbFile.createTime();
    }

    public long getLastModified() throws IOException {
        return smbFile.getLastModified();
    }

    /**
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        return getSimpleName(smbFile);
    }

    static String getSimpleName(SmbFile smbFile) {
        String name = smbFile.getName();
        if (name.endsWith("/"))
            name = name.substring(0, name.length() - 1);
        return name;
    }

    /**
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public SMBFSDirectory getParent() {
        return parent;
    }

    /**
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        try {
            return smbFile.isDirectory();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.jnode.fs.FSEntry#isDirty()
     */
    public boolean isDirty() throws IOException {
        //todo implement it
        return false;
    }

    /**
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        try {
            return smbFile.isFile();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCreated(long created) throws IOException {
        smbFile.setCreateTime(created);
    }

    public void setLastModified(long lastModified) throws IOException {
        smbFile.setLastModified(lastModified);
    }

    /**
     * @see org.jnode.fs.FSEntry#setName(String)
     */
    public void setName(String newName) throws IOException {
        SmbFile f = new SmbFile(smbFile.getParent(), newName);
        smbFile.renameTo(f);
    }

    /**
     * @see org.jnode.fs.FSEntry#getFileSystem()
     */
    public SMBFileSystem getFileSystem() {
        //todo implement it
        return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#isValid()
     */
    public boolean isValid() {
        //todo implement it
        return true;
    }
}
