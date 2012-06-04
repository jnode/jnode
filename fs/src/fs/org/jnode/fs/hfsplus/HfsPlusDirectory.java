/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.hfsplus.catalog.Catalog;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.catalog.CatalogKey;
import org.jnode.fs.hfsplus.catalog.CatalogLeafNode;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.FSEntryTable;

public class HfsPlusDirectory implements FSDirectory {

    private static final Logger log = Logger.getLogger(HfsPlusDirectory.class);

    /** The directory entry */
    private HfsPlusEntry entry;

    /** Table of entries of our parent */
    private FSEntryTable entries;

    /** The catalog directory record */
    private CatalogFolder folder;

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
     * 
     * @return read all entries link to the current directory in the file system.
     * @throws IOException
     */
    private FSEntryTable readEntries() throws IOException {
        List<FSEntry> pathList = new LinkedList<FSEntry>();
        HfsPlusFileSystem fs = (HfsPlusFileSystem) getFileSystem();
        if (fs.getVolumeHeader().getFolderCount() > 0) {
            LeafRecord[] records = fs.getCatalog().getRecords(folder.getFolderId());
            for (LeafRecord rec : records) {
                if (rec.getType() == CatalogFolder.RECORD_TYPE_FOLDER ||
                        rec.getType() == CatalogFile.RECORD_TYPE_FILE) {
                    String name = ((CatalogKey) rec.getKey()).getNodeName().getUnicodeString();
                    HfsPlusEntry e = new HfsPlusEntry(fs, this, name, rec);
                    pathList.add(e);
                }
            }
        }
        return new FSEntryTable(((HfsPlusFileSystem) getFileSystem()), pathList);
    }

    private void writeEntries(final FSEntryTable entries) throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * 
     * @param name The name of the entry.
     * @return  Return the newly created entry.
     * @throws IOException if problem occurs during catalog node creation or if system is read-only.
     */
    private FSEntry createDirectoryEntry(final String name) throws IOException {
        if (getFileSystem().isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        Catalog catalog = ((HfsPlusFileSystem) getFileSystem()).getCatalog();
        SuperBlock volumeHeader = ((HfsPlusFileSystem) getFileSystem()).getVolumeHeader();
        CatalogLeafNode node =
                catalog.createNode(name, this.folder.getFolderId(),
                        new CatalogNodeId(volumeHeader.getNextCatalogId()),
                        CatalogFolder.RECORD_TYPE_FOLDER_THREAD);
        folder.incrementValence();

        HfsPlusEntry newEntry = new HfsPlusEntry((HfsPlusFileSystem) getFileSystem(), this, name, node.getNodeRecord(0));
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
    public FileSystem<?> getFileSystem() {
        return entry.getFileSystem();
    }
}
