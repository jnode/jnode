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
 
package org.jnode.fs.ftpfs;

import org.jnode.fs.FSFile;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.enterprisedt.net.ftp.FTPFile;


/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSFile extends FTPFSEntry implements FSFile {
    private byte[] data;

    FTPFSFile(FTPFileSystem fileSystem, FTPFile ftpFile) {
        super(fileSystem, ftpFile);
    }

    /**
     * Gets the length (in bytes) of this file
     *
     * @return long
     */
    public long getLength() {
        return ftpFile.size();
    }

    /**
     * Read <code>len</code> bytes from the given position.
     * The read data is read fom this file starting at offset <code>fileOffset</code>
     * and stored in <code>dest</code> starting at offset <code>ofs</code>.
     *
     * @param fileOffset
     * @param dest
     * @throws java.io.IOException
     */
    public synchronized void read(long fileOffset, ByteBuffer dest) throws IOException {
        try {
            if (data == null) {
                synchronized (fileSystem) {
                    fileSystem.chdir(parent.path());
                    data = fileSystem.get(getName());
                    //InputStream in = fileSystem.retrieveFileStream(getName());
                    //int i = in.available();
                    //data = new byte[i];
                    //in.read(data);

                }
            }
            int len = dest.remaining();
            len = Math.min(len, (int) (data.length - fileOffset));
            if (len > 0) {
                dest.put(data, (int) fileOffset, len);
            }
        } catch (Exception e) {
            throw new IOException("Read error");
        }
    }

    /**
     * Flush any cached data to the disk.
     *
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        // TODO implement me
    }

    /**
     * Sets the length of this file.
     *
     * @param length
     * @throws java.io.IOException
     */
    public void setLength(long length) throws IOException {
        // TODO implement me
    }

    /**
     * Write <code>len</code> bytes to the given position.
     * The data is read from <code>src</code> starting at offset
     * <code>ofs</code> and written to this file starting at offset <code>fileOffset</code>.
     *
     * @param fileOffset
     * @param src
     * @throws java.io.IOException
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {
        // TODO implement me
    }
}
