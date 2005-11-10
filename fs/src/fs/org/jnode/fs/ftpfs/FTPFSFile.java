package org.jnode.fs.ftpfs;

import org.jnode.fs.FSFile;
import org.jnode.fs.FSEntry;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPException;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSFile extends FTPFSEntry implements FSFile, FSEntry {
    private byte[] data;
    public FTPFSFile(FTPFileSystem fileSystem, FTPFile ftpFile) {
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
            if(data == null){
                synchronized(fileSystem) {
                    fileSystem.chdir(parent.path());
                    data = fileSystem.get(getName());
                }
            }
            int len = dest.remaining();
            len = Math.min(len,(int) (data.length - fileOffset));
            if(len > 0){
                dest.put(data, (int) fileOffset, len);
            }
        }catch(FTPException e){
            throw new IOException("Read error");
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
