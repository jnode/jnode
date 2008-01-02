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

import org.jnode.fs.FSFile;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.ReadFileResult;
import org.jnode.net.nfs.nfs2.Time;

/**
 * @author Andrei Dore
 */
public class NFS2File extends NFS2Object implements FSFile {

    private NFS2Entry entry;

    NFS2File(NFS2Entry entry) {
        super((NFS2FileSystem) entry.getFileSystem());
        this.entry = entry;
    }

    /**
     * Gets the length (in bytes) of this file
     * 
     * @return long
     */
    public long getLength() {
        return entry.getFileAttribute().getSize();
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

        NFS2Client client = getNFS2Client();

        try {

            int length = NFS2Client.MAX_DATA;

            while (length == NFS2Client.MAX_DATA) {

                length = Math.min(NFS2Client.MAX_DATA, dest.remaining());

                ReadFileResult result = client.readFile(entry.getFileHandle(),
                        (int) fileOffset, length);

                byte[] data = result.getData();

                length = data.length;

                fileOffset += length;

                dest.put(data);
            }

        } catch (NFS2Exception e) {
            throw new IOException("Error reading file. Reason:"
                    + e.getMessage(), e);
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

        NFS2Client client = getNFS2Client();

        try {
            client.setAttribute(entry.getFileHandle(), -1, -1, -1,
                    (int) length, new Time(-1, -1), new Time(-1, -1));
        } catch (NFS2Exception e) {
            throw new IOException(e.getMessage(), e);
        }

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

        NFS2Client client = getNFS2Client();

        try {

            byte[] data = new byte[NFS2Client.MAX_DATA];

            int count;

            while (src.remaining() > 0) {

                count = Math.min(NFS2Client.MAX_DATA, src.remaining());

                src.get(data, 0, count);

                FileAttribute fileAttribute = client.writeFile(entry
                        .getFileHandle(), (int) fileOffset, data, 0, count);

                fileOffset += count;

            }

        } catch (NFS2Exception e) {
            throw new IOException("Error writing file . Reason: "
                    + e.getMessage(), e);
        }

    }

}
