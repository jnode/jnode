/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660File implements FSFile {

    private final ISO9660Entry entry;

    /**
     * @param entry
     */
    public ISO9660File(ISO9660Entry entry) {
        this.entry = entry;
    }

    /**
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        return entry.getCDFSentry().getDataLength();
    }

    /**
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
     */
//    public void read(long fileOffset, byte[] dest, int off, int len)
    public void read(long fileOffset, ByteBuffer destBuf)    
            throws IOException {
        //TODO optimize it also to use ByteBuffer at lower level
        final int len = destBuf.remaining();
        final int off = destBuf.position();
        final byte[] dest = destBuf.array();
        this.entry.getCDFSentry().readFileData(fileOffset, dest, off, len);
    }

    /**
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    //public void write(long fileOffset, byte[] src, int off, int len)
    public void write(long fileOffset, ByteBuffer src)
            throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSFile#flush()
     */
    public void flush() throws IOException {
        // Readonly
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public final FileSystem getFileSystem() {
        return entry.getFileSystem();
    }
}
