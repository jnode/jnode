package org.jnode.fs.smbfs;

import org.jnode.fs.FSFile;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcifs.smb.SmbFile;

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
        byte[] data = new byte[(int) getLength()];
        smbFile.getInputStream().read(data);
        dest.put(data, (int) fileOffset, dest.remaining());
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
