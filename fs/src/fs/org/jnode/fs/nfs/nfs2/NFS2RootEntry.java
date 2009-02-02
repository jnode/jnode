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
 
package org.jnode.fs.nfs.nfs2;

import java.io.IOException;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.net.nfs.nfs2.FileAttribute;

public class NFS2RootEntry extends NFS2Entry implements FSEntry {
    NFS2RootEntry(NFS2FileSystem fileSystem, byte[] fileHandle, FileAttribute fileAttribute) {
        super(fileSystem, null, "/", fileHandle, fileAttribute);
    }

    public FSFile getFile() throws IOException {
        throw new IOException("It is not  a file. It is the root of the file system.");
    }

    public void setLastModified(long lastModified) throws IOException {
        throw new IOException("Cannot change last modified of root directory");

    }

    public void setName(String newName) throws IOException {
        throw new IOException("Cannot change name of root directory");
    }

}
