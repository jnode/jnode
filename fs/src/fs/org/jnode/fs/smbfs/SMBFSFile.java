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
 
package org.jnode.fs.smbfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;
import org.jnode.fs.FSFile;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSFile extends SMBFSEntry implements FSFile {
    private static final int BUFFER_SIZE = 4 * 1024;

    protected SMBFSFile(SMBFSDirectory parent, SmbFile smbFile) {
        super(parent, smbFile);
    }

    /**
     * @see org.jnode.fs.FSFile#flush()
     */
    public void flush() throws IOException {

    }

    /**
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        try {
            return smbFile.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.jnode.fs.FSFile#read(long, java.nio.ByteBuffer)
     */
    public void read(long fileOffset, ByteBuffer dest) throws IOException {
        if (fileOffset > smbFile.length())
            return;

        byte[] buf = new byte[BUFFER_SIZE];

        SmbRandomAccessFile raf = new SmbRandomAccessFile(smbFile, "r");

        raf.seek(fileOffset);

        int bc;
        int rem = dest.remaining();
        while ((bc = raf.read(buf)) > 0 && rem > 0) {
            dest.put(buf, 0, Math.min(bc, dest.remaining()));
            rem = dest.remaining();
        }
        raf.close();
    }

    /**
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {
        SmbRandomAccessFile raf = new SmbRandomAccessFile(smbFile, "rw");
        raf.setLength(length);
        raf.close();
    }

    /**
     * @see org.jnode.fs.FSFile#write(long, java.nio.ByteBuffer)
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {
        SmbRandomAccessFile raf = new SmbRandomAccessFile(smbFile, "rw");

        if (fileOffset > raf.length())
            raf.setLength(fileOffset);

        raf.seek(fileOffset);

        byte[] buf = new byte[BUFFER_SIZE];

        while (src.remaining() > 0) {
            int bc = Math.min(BUFFER_SIZE, src.remaining());
            src.get(buf, 0, bc);
            raf.write(buf, 0, bc);
        }

        raf.close();
    }
}
