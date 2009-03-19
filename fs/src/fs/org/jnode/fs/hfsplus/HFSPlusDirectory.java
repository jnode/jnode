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

package org.jnode.fs.hfsplus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.catalog.CatalogKey;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.catalog.CatalogThread;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusDirectory extends HFSPlusEntry implements FSDirectory {

    private static final Logger log = Logger.getLogger(HFSPlusDirectory.class);

    /** Table of entries of our parent */
    private FSEntryTable entries;

    private CatalogFolder folder;

    public HFSPlusDirectory(HfsPlusFileSystem fs, HFSPlusDirectory parent, String name,
            LeafRecord record) {
        super(fs, parent, name, record);
        this.folder = new CatalogFolder(record.getData());
        this.entries = FSEntryTable.EMPTY_TABLE;
    }

    @Override
    public FSEntry addDirectory(String name) throws IOException {
        log.debug("<<< BEGIN addDirectory " + name + " >>>");
        if (fs.isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }

        if (getEntry(name) != null) {
            throw new IOException("File or Directory already exists" + name);
        }
        FSEntry newEntry = createDirectoryEntry(name);
        setFreeEntry(newEntry);
        log.debug("<<< END addDirectory " + name + " >>>");
        return newEntry;
    }

    @Override
    public FSEntry addFile(String name) throws IOException {
        log.debug("<<< BEGIN addFile " + name + " >>>");
        if (fs.isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        if (getEntry(name) != null) {
            throw new IOException("File or directory already exists: " + name);
        }
        FSEntry newEntry = createFileEntry(name);
        setFreeEntry(newEntry);

        log.debug("<<< END addFile " + name + " >>>");
        return newEntry;
    }

    private final FSEntry createFileEntry(final String name) throws IOException {
        // TODO
        return null;
    }

    @Override
    public void flush() throws IOException {
        log.debug("<<< BEGIN flush >>>");
        if (fs.isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        boolean flushEntries = isEntriesLoaded() && entries.isDirty();
        if (isDirty() || flushEntries) {
            writeEntries(entries);
            // entries.resetDirty();
            resetDirty();
        }
        log.debug("<<< END flush >>>");
    }

    @Override
    public FSEntry getEntry(String name) throws IOException {
        checkEntriesLoaded();
        return entries.get(name);
    }

    @Override
    public Iterator<? extends FSEntry> iterator() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public int rename(String oldName, String newName) {
        return entries.rename(oldName, newName);
    }

    @Override
    public void remove(String name) throws IOException {
        if (fs.isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }
        if (entries.remove(name) >= 0) {
            setDirty();
            flush();
            return;
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
                if (rights.canRead()) {
                    entries = readEntries();
                } else {
                    // the next time, we will call checkEntriesLoaded()
                    // we will retry to load entries
                    entries = FSEntryTable.EMPTY_TABLE;
                    log.debug("checkEntriesLoaded : can't read, using EMPTY_TABLE");
                }
                resetDirty();
            } catch (IOException e) {
                log.fatal("unable to read directory entries", e);
                // the next time, we will call checkEntriesLoaded()
                // we will retry to load entries
                entries = FSEntryTable.EMPTY_TABLE;
            }
        }
        log.debug("<<< END checkEntriesLoaded >>>");
    }

    /**
     * Have we already loaded our entries from device ?
     * 
     * @return if the entries are already loaded from the device
     */
    private final boolean isEntriesLoaded() {
        return (entries != FSEntryTable.EMPTY_TABLE);
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    private final FSEntryTable readEntries() throws IOException {
        List<FSEntry> pathList = new LinkedList<FSEntry>();
        HfsPlusFileSystem fs = (HfsPlusFileSystem) getFileSystem();
        if (fs.getVolumeHeader().getFolderCount() > 0) {
            LeafRecord[] records = fs.getCatalog().getRecords(folder.getFolderId());
            for (LeafRecord rec : records) {
                if (rec.getType() == CatalogFolder.RECORD_TYPE_FOLDER ||
                        rec.getType() == CatalogFile.RECORD_TYPE_FILE) {
                    String name = ((CatalogKey) rec.getKey()).getNodeName().getUnicodeString();
                    HFSPlusEntry e = new HFSPlusDirectory(fs, this, name, rec);
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
     * @param name
     * @return
     * @throws IOException
     */
    private final FSEntry createDirectoryEntry(final String name) throws IOException {
        if (fs.isReadOnly()) {
            throw new ReadOnlyFileSystemException();
        }

        Superblock volumeHeader = ((HfsPlusFileSystem) getFileSystem()).getVolumeHeader();
        HFSUnicodeString dirName = new HFSUnicodeString(name);
        CatalogThread thread =
                new CatalogThread(CatalogFolder.RECORD_TYPE_FOLDER_THREAD, this.folder
                        .getFolderId(), dirName);
        CatalogFolder newFolder =
                new CatalogFolder(0, new CatalogNodeId(volumeHeader.getNextCatalogId()));
        log.debug("New catalog folder :\n" + newFolder.toString());

        CatalogKey key = new CatalogKey(this.folder.getFolderId(), dirName);
        log.debug("New catalog key :\n" + key.toString());

        LeafRecord folderRecord = new LeafRecord(key, newFolder.getBytes());
        log.debug("New record folder :\n" + folderRecord.toString());

        HFSPlusEntry newEntry = new HFSPlusDirectory(fs, this, name, folderRecord);
        volumeHeader.setFolderCount(volumeHeader.getFolderCount() + 1);
        log.debug("New volume header :\n" + volumeHeader.toString());

        return newEntry;
    }

    /**
     * Find a free entry and set it with the given entry
     * 
     * @param newEntry
     * @throws IOException
     */
    private final void setFreeEntry(FSEntry newEntry) throws IOException {
        checkEntriesLoaded();
        if (entries.setFreeEntry(newEntry) >= 0) {
            log.debug("setFreeEntry: free entry found !");
            setDirty();
            flush();
            return;
        }
    }

}
