/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSEntry implements FSEntry {

    private FSObject cachedFSObject;

    private final IndexEntry indexEntry;

    private final NTFSFileSystem fs;

    /**
     * Initialize this instance.
     */
    public NTFSEntry(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
    }

    /**
     * Gets the name of this entry.
     * 
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        if (indexEntry != null) return indexEntry.getFileName();
        return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public FSDirectory getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#getLastModified()
     */
    public long getLastModified() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        return !indexEntry.isDirectory();
    }

    /**
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        return indexEntry.isDirectory();
    }

    /**
     * @see org.jnode.fs.FSEntry#setName(java.lang.String)
     */
    public void setName(String newName) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.fs.FSEntry#setLastModified(long)
     */
    public void setLastModified(long lastModified) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public FSFile getFile() {
        if (this.isFile()) {
            if (cachedFSObject == null) {
                cachedFSObject = new NTFSFile(indexEntry);
            }
            return (FSFile) cachedFSObject;
        } else {
            return null;
        }
    }

    /**
     * @see org.jnode.fs.FSEntry#getDirectory()
     */
    public FSDirectory getDirectory() throws IOException {
        if (this.isDirectory()) {
            if (cachedFSObject == null) {
                cachedFSObject = new NTFSDirectory(fs, getFileRecord()
                        .getVolume().getMFT().getIndexedFileRecord(indexEntry));
            }
            return (FSDirectory) cachedFSObject;
        } else
            return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#getAccessRights()
     */
    public FSAccessRights getAccessRights() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem getFileSystem() {
        return fs;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() throws IOException {
        return indexEntry.getParentFileRecord().getVolume().getMFT()
                .getIndexedFileRecord(indexEntry);
    }

    /**
     * @return Returns the indexEntry.
     */
    public IndexEntry getIndexEntry() {
        return indexEntry;
    }

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     * 
     * @return true if the entry need to be saved
     * @throws IOException
     */
    public boolean isDirty() throws IOException {
        return true;
    }
}