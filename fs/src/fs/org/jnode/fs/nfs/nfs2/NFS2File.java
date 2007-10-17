/*
 * $Id: FTPFSFile.java 2406 2006-03-23 06:17:24Z lsantha $
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
import java.nio.ByteBuffer;

import org.acplt.oncrpc.OncRpcException;
import org.jnode.fs.FSFile;
import org.jnode.fs.nfs.nfs2.rpc.nfs.FileAttribute;
import org.jnode.fs.nfs.nfs2.rpc.nfs.NFS2Client;
import org.jnode.fs.nfs.nfs2.rpc.nfs.ReadFileResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.Status;

/**
 * @author Andrei Dore
 */
public class NFS2File extends NFS2Object implements FSFile {

    private byte[] fileHandle;
    private FileAttribute fileAttribute;

    NFS2File(NFS2FileSystem fileSystem, byte[] fileHandle, FileAttribute fileAttribute) {
        super(fileSystem);
        this.fileHandle = fileHandle;
        this.fileAttribute = fileAttribute;
    }

    /**
     * Gets the length (in bytes) of this file
     *
     * @return long
     */
    public long getLength() {
        return fileAttribute.getSize();
    }

    /**
     * Read <code>len</code> bytes from the given position. The read data is
     * read fom this file starting at offset <code>fileOffset</code> and
     * stored in <code>dest</code> starting at offset <code>ofs</code>.
     *
     * @param fileOffset
     * @param dest
     * @throws java.io.IOException
     */
    public void read(long fileOffset, ByteBuffer dest) throws IOException {

        NFS2Client client = getNFSClient();

        try {
            ReadFileResult result = client.readFile(fileHandle, (int) fileOffset, dest.remaining());

            if (result.getStatus() == Status.NFS_OK) {
                fileAttribute = result.getFileAttribute();
                dest.put(result.getData());
            } else {
                throw new IOException("Error " + result.getStatus());
            }

        } catch (OncRpcException e) {
            throw new IOException(e.getMessage(), e);
        }

    }

    /**
     * Flush any cached data to the disk.
     *
     * @throws java.io.IOException
     */
    public void flush() throws IOException {

    }

    /**
     * Sets the length of this file.
     *
     * @param length
     * @throws java.io.IOException
     */
    public void setLength(long length) throws IOException {

    }

    /**
     * Write <code>len</code> bytes to the given position. The data is read
     * from <code>src</code> starting at offset <code>ofs</code> and written
     * to this file starting at offset <code>fileOffset</code>.
     *
     * @param fileOffset
     * @param src
     * @throws java.io.IOException
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {

    }

}
