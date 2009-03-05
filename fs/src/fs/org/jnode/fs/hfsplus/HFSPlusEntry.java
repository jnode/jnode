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

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.spi.UnixFSAccessRights;

public class HFSPlusEntry implements FSEntry {

    protected HfsPlusFileSystem fs;
    protected HFSPlusDirectory parent;
    protected String name;
    protected LeafRecord record;
    private int type;

    protected boolean valid;
    protected boolean dirty;
    protected FSAccessRights rights;
    private long lastModified;

    /**
     * 
     * @param fs
     * @param parent
     * @param name
     * @param record
     */
    public HFSPlusEntry(HfsPlusFileSystem fs, HFSPlusDirectory parent, String name,
            LeafRecord record) {
        this.fs = fs;
        this.parent = parent;
        this.name = name;
        this.record = record;
        this.type = getFSEntryType();
        this.rights = new UnixFSAccessRights(fs);
        this.lastModified = System.currentTimeMillis();
    }

    private int getFSEntryType() {
        int mode = record.getType();
        if ("/".equals(name)) {
            return AbstractFSEntry.ROOT_ENTRY;
        } else if (mode == HfsPlusConstants.RECORD_TYPE_FOLDER) {
            return AbstractFSEntry.DIR_ENTRY;
        } else if (mode == HfsPlusConstants.RECORD_TYPE_FILE) {
            return AbstractFSEntry.FILE_ENTRY;
        } else {
            return AbstractFSEntry.OTHER_ENTRY;
        }
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        return rights;
    }

    @Override
    public FSDirectory getDirectory() throws IOException {
        if (!isFile()) {
            throw new IOException("It is not a Directory");
        }
        return (HFSPlusDirectory) this;
    }

    @Override
    public FSFile getFile() throws IOException {
        if (!isFile()) {
            throw new IOException("It is not a file");
        }
        return (HFSPlusFile) this;
    }

    @Override
    public long getLastModified() throws IOException {
        // TODO Auto-generated method stub
        return lastModified;
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

    @Override
    public void setLastModified(long lastModified) throws IOException {
        this.lastModified = lastModified;
    }

    @Override
    public void setName(String newName) throws IOException {
        if (type == AbstractFSEntry.ROOT_ENTRY) {
            throw new IOException("Cannot change name of root directory");
        }
        if (parent.getTable().rename(name, newName) < 0) {
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

}
