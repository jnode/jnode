/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

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
    public void read(long fileOffset, byte[] dest, int off, int len)
            throws IOException {
        getFileRecord().readData(fileOffset, dest, off, len);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */
    public void write(long fileOffset, byte[] src, int off, int len) {
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
                fileRecord = indexEntry.getParentFileRecord().getVolume()
                        .getMFT().getIndexedFileRecord(indexEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.fileRecord;
    }

    /**
     * @param fileRecord
     *            The fileRecord to set.
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