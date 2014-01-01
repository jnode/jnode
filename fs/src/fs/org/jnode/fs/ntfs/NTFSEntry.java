/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSEntryLastChanged;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.index.IndexEntry;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSEntry implements FSEntry, FSEntryCreated, FSEntryLastChanged, FSEntryLastAccessed {

    private FSObject cachedFSObject;

    /**
     * The ID for this entry.
     */
    private String id;

    /**
     * The index entry.
     */
    private IndexEntry indexEntry;

    /**
     * The associated file record.
     */
    private FileRecord fileRecord;

    /**
     * The containing file system.
     */
    private final NTFSFileSystem fs;

    /**
     * Initialize this instance.
     *
     * @param fs         the file system.
     * @param indexEntry the index entry.
     */
    public NTFSEntry(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
        id = Long.toString(indexEntry.getFileReferenceNumber());
    }

    /**
     * Initialize this instance.
     *
     * @param fs         the file system.
     * @param fileRecord the file record.
     */
    public NTFSEntry(NTFSFileSystem fs, FileRecord fileRecord) {
        this.fs = fs;
        this.fileRecord = fileRecord;
        id = Long.toString(fileRecord.getReferenceNumber());
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the name of this entry.
     *
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        if (indexEntry != null) {
            return indexEntry.getFileName();
        } else if (fileRecord != null) {
            return fileRecord.getFileName();
        }
        return null;
    }

    /**
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public FSDirectory getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getCreated() throws IOException {
        return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getCreationTime());
    }

    public long getLastModified() throws IOException {
        return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getModificationTime());
    }

    public long getLastChanged() throws IOException {
        return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getMftChangeTime());
    }

    public long getLastAccessed() throws IOException {
        return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getAccessTime());
    }

    /**
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        if (indexEntry != null) {
            return !indexEntry.isDirectory();
        } else {
            return !fileRecord.isDirectory();
        }
    }

    /**
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        if (indexEntry != null) {
            return indexEntry.isDirectory();
        } else {
            return fileRecord.isDirectory();
        }
    }

    /**
     * @see org.jnode.fs.FSEntry#setName(java.lang.String)
     */
    public void setName(String newName) {
        // TODO Auto-generated method stub

    }

    public void setCreated(long created) {
        // TODO: Implement write support.
    }

    public void setLastModified(long lastModified) {
        // TODO: Implement write support.
    }

    public void setLastAccessed(long lastAccessed) {
        // TODO: Implement write support.
    }

    /**
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public FSFile getFile() {
        if (this.isFile()) {
            if (cachedFSObject == null) {
                if (indexEntry != null) {
                    cachedFSObject = new NTFSFile(fs, indexEntry);
                } else {
                    cachedFSObject = new NTFSFile(fs, fileRecord);
                }
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
                if (fileRecord != null) {
                    cachedFSObject = new NTFSDirectory(fs, fileRecord);
                } else {
                    // XXX: Why can't this just use getFileRecord()?
                    cachedFSObject = new NTFSDirectory(fs, getFileRecord().getVolume().getMFT().getIndexedFileRecord(
                        indexEntry));
                }
            }
            return (FSDirectory) cachedFSObject;
        } else return null;
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
    public FileSystem<?> getFileSystem() {
        return fs;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() throws IOException {
        if (fileRecord != null) {
            return fileRecord;
        }
        return indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(indexEntry);
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

    @Override
    public String toString() {
        Object obj = indexEntry == null ? fileRecord : indexEntry;
        return super.toString() + '(' + obj + ')';
    }

}
