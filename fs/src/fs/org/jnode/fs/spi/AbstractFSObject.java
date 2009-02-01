/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.spi;

import java.io.IOException;

import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;

/**
 * An abstract implementation of FSObject that contains common things among many
 * FileSystems
 * 
 * @author Fabien DUMINY
 */
public abstract class AbstractFSObject implements FSObject {

    private AbstractFileSystem fileSystem;

    private boolean valid;
    private boolean dirty;

    // should use FSAccessRights for these fields
    private boolean canRead = true;
    private boolean canWrite = true;

    /**
     * Create a new AbstracFSObject
     * 
     * @param fs
     */
    public AbstractFSObject(AbstractFileSystem fs) {
        this.fileSystem = fs;
        this.valid = true;
        this.dirty = false;
        setRights(true, (fs != null) ? !fs.isReadOnly() : true);
    }

    /**
     * Use it carefully ! Only needed for FSEntryTable.EMPTY_TABLE.
     */
    protected AbstractFSObject() {
        this.fileSystem = null;
        this.valid = true;
        this.dirty = false;
        setRights(true, true);
    }

    /**
     * Is this object still valid ?
     * 
     * @return if this object is still valid
     */
    public final boolean isValid() {
        return valid;
    }

    /**
     * Set the valid flag.
     * 
     * @param valid
     */
    protected final void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Is this object dirty (ie some data need to be saved to device)
     * 
     * @return if this object is dirty
     * @throws IOException
     */
    public boolean isDirty() throws IOException {
        return dirty;
    }

    /**
     * Mark this object as dirty.
     */
    protected final void setDirty() {
        this.dirty = true;
    }

    /**
     * Mark this object as not dirty.
     */
    protected final void resetDirty() {
        this.dirty = false;
    }

    /**
     * Get the file system that this object belong to
     * 
     * @return the FileSystem this object belongs to
     */
    public final FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Specify the rights on this object
     * 
     * @param read
     * @param write
     */
    public final void setRights(boolean read, boolean write) {
        this.canRead = read;
        this.canWrite = write;
    }

    /**
     * Can we read this object on device ?
     * 
     * @return if we can read this object from device
     */
    public final boolean canRead() {
        return this.canRead;
    }

    /**
     * Can we write this object on device ?
     * @return if we can write this object to device
     */
    public final boolean canWrite() {
        return this.canWrite;
    }
}
