/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

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
    public void read(long fileOffset, byte[] dest, int off, int len)
            throws IOException {
        this.entry.getCDFSentry().readFileData(fileOffset, dest, off, len);
    }

    /**
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    public void write(long fileOffset, byte[] src, int off, int len)
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