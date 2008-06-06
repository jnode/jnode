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
 
package org.jnode.fs.smbfs;

import org.jnode.fs.FSFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSFile extends SMBFSEntry implements FSFile {

    protected SMBFSFile(SMBFSDirectory parent, SmbFile smbFile) {
        super(parent, smbFile);
    }

    /**
     * Flush any cached data to the disk.
     *
     * @throws java.io.IOException
     */
    public void flush() throws IOException {

    }

    /**
     * Gets the length (in bytes) of this file
     *
     * @return long
     */
    public long getLength() {
        return smbFile.getContentLength();
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
    public void read(long fileOffset, ByteBuffer dest) throws IOException {
        if(fileOffset > smbFile.length())
            return;

        int b_len = 32 * 1024;
        byte[] buf = new byte[b_len];

        SmbFileInputStream is = (SmbFileInputStream) smbFile.getInputStream();

        long s = is.skip(fileOffset);
        if(s < fileOffset)
            is.skip(fileOffset);

        int bc;
        int rem = 1;
        while((bc = is.read(buf)) > 0 && rem > 0){
            dest.put(buf, 0, Math.min(bc, dest.remaining()));
            rem = dest.remaining();
        }
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
     * Write <code>len</code> bytes to the given position.
     * The data is read from <code>src</code> starting at offset
     * <code>ofs</code> and written to this file starting at offset <code>fileOffset</code>.
     *
     * @param fileOffset
     * @param src
     * @throws java.io.IOException
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {

    }
}
