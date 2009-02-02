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
 
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.util.ByteBufferUtils;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFile implements FSFile {

    private FileRecord fileRecord;

    private final IndexEntry indexEntry;

    /**
     * Initialize this instance.
     * 
     * @param indexEntry
     */
    public NTFSFile(IndexEntry indexEntry) {
        this.indexEntry = indexEntry;
    }

    public long getLength() {
        return indexEntry.getRealFileSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
     */
    // public void read(long fileOffset, byte[] dest, int off, int len)
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();
        getFileRecord().readData(fileOffset, dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    // public void write(long fileOffset, byte[] src, int off, int len) {
    public void write(long fileOffset, ByteBuffer src) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem getFileSystem() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() {
        if (fileRecord == null) {
            try {
                fileRecord =
                        indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(
                                indexEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.fileRecord;
    }

    /**
     * @param fileRecord The fileRecord to set.
     */
    public void setFileRecord(FileRecord fileRecord) {
        this.fileRecord = fileRecord;
    }

    /**
     * Flush any cached data to the disk.
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        // TODO implement me
    }
}
