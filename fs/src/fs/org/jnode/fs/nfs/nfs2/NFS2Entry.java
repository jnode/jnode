/*
 * $Id: FTPFSEntry.java 2260 2006-01-22 11:10:07Z lsantha $
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

package org.jnode.fs.nfs.nfs2;

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.nfs.nfs2.rpc.nfs.FileAttribute;
import org.jnode.fs.nfs.nfs2.rpc.nfs.FileType;

/**
 * @author Andrei Dore
 */
public class NFS2Entry extends NFS2Object implements FSEntry {

    private NFS2Directory parent;

    private NFS2Directory directory;

    private NFS2File file;

    private byte[] fileHandle;

    private FileAttribute fileAttribute;

    private String name;

    NFS2Entry(NFS2FileSystem fileSystem, NFS2Directory parent, String name, byte[] fileHandle,
              FileAttribute fileAttribute) {
        super(fileSystem);

        this.parent = parent;
        this.name = name;
        this.fileAttribute = fileAttribute;

        if (fileAttribute.getType() == FileType.DIRECTORY) {
            directory = new NFS2Directory(fileSystem, fileHandle);

        } else if (fileAttribute.getType() == FileType.FILE) {
            file = new NFS2File(fileSystem, fileHandle, fileAttribute);

        }

    }

    public FSDirectory getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public FSAccessRights getAccessRights() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FSDirectory getDirectory() throws IOException {
        if (!isDirectory()) {
            throw new IOException(getName() + " is not a directory");
        }

        return directory;
    }

    public FSFile getFile() throws IOException {
        if (!isFile()) {
            throw new IOException(getName() + " is not a file");
        }

        return file;
    }

    public long getLastModified() throws IOException {
        return fileAttribute.getLastModified().getMicroSeconds();
    }

    public boolean isDirectory() {

        if (fileAttribute.getType() == FileType.DIRECTORY) {
            return true;
        }

        return false;
    }

    public boolean isDirty() throws IOException {
        return false;
    }

    public boolean isFile() {

        if (fileAttribute.getType() == FileType.FILE) {
            return true;
        }

        return false;
    }

    public void setLastModified(long lastModified) throws IOException {
        // TODO Auto-generated method stub

    }

    public void setName(String newName) throws IOException {
        // TODO Auto-generated method stub

    }

}
