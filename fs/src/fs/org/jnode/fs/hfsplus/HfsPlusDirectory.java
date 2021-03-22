/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.hfsplus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.hfsplus.catalog.Catalog;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.catalog.CatalogKey;
import org.jnode.fs.hfsplus.catalog.CatalogLeafNode;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.FSEntryTable;

public class HfsPlusDirectory implements FSDirectory, FSDirectoryId {

    private static final Logger log = Logger.getLogger(HfsPlusDirectory.class);

    /**
     * The directory entry
     */
    private HfsPlusEntry entry;

    /**
     * Table of entries of our parent
     */
    private FSEntryTable entries;

    /**
     * The catalog directory record
     */
    private CatalogFolder folder;

    /**
     * The hardlink directory which contains the actual directory contents if this directory is a hard link.
     */
    private CatalogFolder hardLinkFolder;

    public HfsPlusDirectory(HfsPlusEntry entry) {
        this.entry = entry;
        this.folder = new CatalogFolder(entry.getData());
        this.entries = FSEntryTable.EMPTY_TABLE;
    }

    @Override
    public FSEntry addDirectory(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }

        if (getEntry(name) != null) {
            throw new IOException("File or Directory already exists : " + name);
        }
        FSEntry newEntry = createDirectoryEntry(name);
        setFreeEntry(newEntry);
        log.debug("Directory " + name + " added");
        return newEntry;
    }

    @Override
    public FSEntry addFile(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        if (getEntry(name) != null) {
            throw new IOException("File or directory already exists: " + name);
        }
        FSEntry newEntry = createFileEntry(name);
        setFreeEntry(newEntry);

        return newEntry;
    }

    private FSEntry createFileEntry(final String name) throws IOException {
        //TODO implements this method.
        /*
         * if (fs.isReadOnly()) { throw new ReadOnlyFileSystemException(); }
         * Catalog catalog = fs.getCatalog(); SuperBlock volumeHeader =
         * ((HfsPlusFileSystem) getFileSystem()).getVolumeHeader(); LeafRecord
         * fileRecord = catalog.createNode(name, this.folder .getFolderId(), new
         * CatalogNodeId(volumeHeader.getNextCatalogId()),
         * CatalogFile.RECORD_TYPE_FILE);
         * 
         * HFSPlusEntry newEntry = new HFSPlusFile(fs, this, name,
         * folderRecord); newEntry.setDirty();
         * volumeHeader.setFileCount(volumeHeader.getFileCount() + 1);
         * log.debug("New volume header :\n" + volumeHeader.toString());
         */

        return null;
    }

    @Override
    public void flush() throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        boolean flushEntries = isEntriesLoaded() && entries.isDirty();
        if (entry.isDirty() || flushEntries) {
            writeEntries(entries);
            // entries.resetDirty();
            entry.resetDirty();
        }
        log.debug("Directory flushed.");
    }

    @Override
    public FSEntry getEntry(String name) throws IOException {
        checkEntriesLoaded();
        return entries.get(name);
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        checkEntriesLoaded();
        return entries.getById(id);
    }

    @Override
    public String getDirectoryId() {
        return Long.toString(folder.getFolderId().getId());
    }

    @Override
    public Iterator<? extends FSEntry> iterator() throws IOException {
        checkEntriesLoaded();
        return entries.iterator();
    }

    public int rename(String oldName, String newName) {
        return entries.rename(oldName, newName);
    }

    @Override
    public void remove(String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        if (entries.remove(name) >= 0) {
            entry.setDirty();
            flush();
        } else {
            throw new FileNotFoundException(name);
        }
    }

    // Helper methods

    /**
     * BE CAREFULL : don't call this method from the constructor of this class
     * because it call the method readEntries of the child classes that are not
     * yet initialized (constructed).
     */
    protected final void checkEntriesLoaded() {
        if (!isEntriesLoaded()) {
            log.debug("checkEntriesLoaded : loading");
            try {
                if (entry.getAccessRights().canRead()) {
                    entries = readEntries();
                    log.debug("Load " + entries.size() + " entrie(s).");
                } else {
                    // the next time, we will call checkEntriesLoaded()
                    // we will retry to load entries
                    entries = FSEntryTable.EMPTY_TABLE;
                    log.debug("checkEntriesLoaded : can't read, using EMPTY_TABLE");
                }
                entry.resetDirty();
            } catch (IOException e) {
                log.fatal("unable to read directory entries", e);
                // the next time, we will call checkEntriesLoaded()
                // we will retry to load entries
                entries = FSEntryTable.EMPTY_TABLE;
            }
        }
    }

    /**
     * Have we already loaded our entries from device ?
     *
     * @return if the entries are already loaded from the device
     */
    private boolean isEntriesLoaded() {
        return (entries != FSEntryTable.EMPTY_TABLE);
    }

    /**
     * @return read all entries link to the current directory in the file system.
     * @throws IOException
     */
    private FSEntryTable readEntries() throws IOException {
        List<FSEntry> pathList = new LinkedList<FSEntry>();
        HfsPlusFileSystem fs = getFileSystem();
        if (fs.getVolumeHeader().getFolderCount() > 0) {
            List<LeafRecord> records;

            if ((folder.getFlags() & CatalogFile.FLAGS_HARDLINK_CHAIN) != 0) {
                records = fs.getCatalog().getRecords(getHardLinkFolder().getFolderId());
            } else {
                records = fs.getCatalog().getRecords(folder.getFolderId());
            }

            for (LeafRecord rec : records) {
                if (rec.getType() == CatalogFolder.RECORD_TYPE_FOLDER ||
                    rec.getType() == CatalogFile.RECORD_TYPE_FILE) {
                    String name = ((CatalogKey) rec.getKey()).getNodeName().getUnicodeString();
                    HfsPlusEntry e = new HfsPlusEntry(fs, this, name, rec);
                    pathList.add(e);
                }
            }
        }
        return new FSEntryTable(getFileSystem(), pathList);
    }

    /**
     * Gets the hard link folder associated with this HFS+ folder.
     *
     * @return the hardlink folder.
     */
    public CatalogFolder getHardLinkFolder() {
        if (hardLinkFolder != null) {
            return hardLinkFolder;
        }

        HfsPlusDirectory privateDirectoryDataDirectory = getFileSystem().getPrivateDirectoryDataDirectory();
        if (privateDirectoryDataDirectory == null) {
            return null;
        }

        if (entry.getParent() != null &&
            ((HfsPlusDirectory) entry.getParent()).getDirectoryId().equals(
                privateDirectoryDataDirectory.getDirectoryId())) {
            // This directory is already under the private directory data directory, so it should be the root in the
            // hardlink chain.
            return folder;
        }

        int flags = folder.getFlags();
        if ((flags & CatalogFile.FLAGS_HARDLINK_CHAIN) == 0) {
            throw new IllegalStateException("Folder is not hard linked");
        }

        // Lookup the CNID for the root of the hardlink chain
        CatalogNodeId hardLinkRoot = new CatalogNodeId(folder.getPermissions().getSpecial());

        try {
            // Lookup the hardlink in the private directory data directory
            String nodeName = "dir_" + hardLinkRoot.getId();
            HfsPlusEntry hardLinkEntry = (HfsPlusEntry) privateDirectoryDataDirectory.getEntry(nodeName);
            hardLinkFolder = hardLinkEntry.createCatalogFolder();
        } catch (IOException e) {
            throw new IllegalStateException("Error looking up hardlink root record: " + hardLinkRoot + " for: " +
                folder);
        }

        return hardLinkFolder;
    }

    private void writeEntries(final FSEntryTable entries) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @param name The name of the entry.
     * @return Return the newly created entry.
     * @throws IOException if problem occurs during catalog node creation or if system is read-only.
     */
    private FSEntry createDirectoryEntry(final String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        Catalog catalog = getFileSystem().getCatalog();
        SuperBlock volumeHeader = getFileSystem().getVolumeHeader();
        CatalogLeafNode node =
            catalog.createNode(name, this.folder.getFolderId(),
                new CatalogNodeId(volumeHeader.getNextCatalogId()),
                CatalogFolder.RECORD_TYPE_FOLDER_THREAD);
        folder.incrementValence();

        HfsPlusEntry newEntry = new HfsPlusEntry(getFileSystem(), this, name, node.getNodeRecord(0));
        newEntry.setDirty();
        volumeHeader.incrementFolderCount();
        log.debug("New volume header :\n" + volumeHeader.toString());
        volumeHeader.update();

        return newEntry;
    }

    /**
     * Find a free entry and set it with the given entry
     *
     * @param newEntry
     * @throws IOException
     */
    private void setFreeEntry(FSEntry newEntry) throws IOException {
        checkEntriesLoaded();
        if (entries.setFreeEntry(newEntry) >= 0) {
            log.debug("setFreeEntry: free entry found !");
            entry.setDirty();
            flush();
        }
    }

    @Override
    public boolean isValid() {
        return entry.isValid();
    }

    @Override
    public HfsPlusFileSystem getFileSystem() {
        return (HfsPlusFileSystem) entry.getFileSystem();
    }
}
