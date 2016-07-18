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

import java.io.IOException;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.spi.UnixFSAccessRights;

public class HfsPlusEntry implements FSEntry, FSEntryCreated, FSEntryLastAccessed {

    protected HfsPlusFileSystem fs;
    protected HfsPlusDirectory parent;
    protected String name;
    protected LeafRecord record;
    private int type;

    protected boolean valid;
    protected boolean dirty;
    protected FSAccessRights rights;

    /**
     * @param fs
     * @param parent
     * @param name
     * @param record
     */
    public HfsPlusEntry(HfsPlusFileSystem fs, HfsPlusDirectory parent, String name,
                        LeafRecord record) {
        this.fs = fs;
        this.parent = parent;
        this.name = name;
        this.record = record;
        this.type = getFSEntryType();
        this.rights = new UnixFSAccessRights(fs);
    }

    private int getFSEntryType() {
        int mode = record.getType();
        if ("/".equals(name)) {
            return AbstractFSEntry.ROOT_ENTRY;
        } else if (mode == CatalogFolder.RECORD_TYPE_FOLDER) {
            return AbstractFSEntry.DIR_ENTRY;
        } else if (mode == CatalogFile.RECORD_TYPE_FILE) {
            CatalogFile catalogFile = new CatalogFile(getData());

            if (catalogFile.getUserInfo().getFileType() == CatalogFolder.HARDLINK_FOLDER_TYPE &&
                catalogFile.getUserInfo().getFileCreator() == CatalogFolder.HARDLINK_CREATOR) {
                // The file type and creator match the folder hardlink constants, this file is a placeholder for a
                // hard-linked directory
                return AbstractFSEntry.DIR_ENTRY;
            } else {
                return AbstractFSEntry.FILE_ENTRY;
            }
        } else {
            return AbstractFSEntry.OTHER_ENTRY;
        }
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        return rights;
    }

    @Override
    public HfsPlusDirectory getDirectory() throws IOException {
        if (!isDirectory()) {
            throw new IOException("It is not a Directory");
        }
        return new HfsPlusDirectory(this);
    }

    @Override
    public FSFile getFile() throws IOException {
        if (!isFile()) {
            throw new IOException("It is not a file");
        }
        return new HfsPlusFile(this);
    }

    @Override
    public long getLastModified() throws IOException {
        if (isFile()) {
            CatalogFile catalogFile = new CatalogFile(getData());
            return catalogFile.getContentModDate();
        } else {
            CatalogFolder catalogFolder = new CatalogFolder(getData());
            return catalogFolder.getContentModDate();
        }
    }

    @Override
    public String getId() {
        try {
            if (isFile()) {
                HfsPlusFile hfsPlusFile = (HfsPlusFile) getFile();
                return Long.toString(hfsPlusFile.getCatalogFile().getFileId().getId());
            } else {
                return getDirectory().getDirectoryId();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error getting ID", e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FSDirectory getParent() {
        return parent;
    }

    @Override
    public boolean isDirectory() {
        return (type == AbstractFSEntry.DIR_ENTRY || type == AbstractFSEntry.ROOT_ENTRY);
    }

    @Override
    public boolean isDirty() throws IOException {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
    }

    public void resetDirty() {
        dirty = false;
    }

    @Override
    public boolean isFile() {
        return (type == AbstractFSEntry.FILE_ENTRY);
    }

    public CatalogFile createCatalogFile() {
        if (!isFile()) {
            throw new IllegalStateException("Attempted to create a catalog file but this entry is not a file!");
        }

        return new CatalogFile(getData());
    }

    public CatalogFolder createCatalogFolder() {
        if (isFile()) {
            throw new IllegalStateException("Attempted to create a catalog folder but this entry is not a directory!");
        }

        return new CatalogFolder(getData());
    }

    @Override
    public void setLastModified(long lastModified) throws IOException {
        if (isFile()) {
            CatalogFile catalogFile = new CatalogFile(getData());
            // catalogFile.setContentModDate();
            throw new UnsupportedOperationException("Not implemented yet.");
        } else {
            CatalogFolder catalogFolder = new CatalogFolder(getData());
            catalogFolder.setContentModDate(lastModified);
        }
    }

    @Override
    public void setName(String newName) throws IOException {
        if (type == AbstractFSEntry.ROOT_ENTRY) {
            throw new IOException("Cannot change name of root directory");
        }
        if (parent.rename(name, newName) < 0) {
            throw new IOException("Cannot change name");
        }

        this.name = newName;
    }

    @Override
    public FileSystem<?> getFileSystem() {
        return fs;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public byte[] getData() {
        return this.record.getData();
    }

    @Override
    public long getCreated() throws IOException {
        if (isFile()) {
            CatalogFile catalogFile = new CatalogFile(getData());
            return catalogFile.getCreateDate();
        } else {
            CatalogFolder catalogFolder = new CatalogFolder(getData());
            return catalogFolder.getCreateDate();
        }
    }

    @Override
    public long getLastAccessed() throws IOException {
        if (isFile()) {
            CatalogFile catalogFile = new CatalogFile(getData());
            return catalogFile.getAccessDate();
        } else {
            CatalogFolder catalogFolder = new CatalogFolder(getData());
            return catalogFolder.getAccessDate();
        }
    }

    @Override
    public final String toString() {
        return String.format("HfsPlusEntry:[cnid:%s %s:'%s']", getId(), isFile() ? "file" : "directory", getName());
    }
}
