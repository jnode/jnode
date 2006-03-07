/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * An abstract implementation of FSObject that contains common things
 * among many FileSystems
 * @author Fabien DUMINY
 */
public abstract class AbstractFSObject implements FSObject {
	public AbstractFSObject(AbstractFileSystem fs)
	{
		this.fileSystem = fs;
		this.valid = true;
		this.dirty = false;
		setRights(true, (fs != null) ? !fs.isReadOnly() : true);
	}

	/**
	 * Use it carefully ! Only needed for FSEntryTable.EMPTY_TABLE.
	 *
	 */
	protected AbstractFSObject()
	{
		this.fileSystem = null;
		this.valid = true;
		this.dirty = false;
		setRights(true, true);
	}

	/**
	 * Is this object still valid ?
	 */
	final public boolean isValid() {
		return valid;
	}

    /**
     * Set the valid flag.
     * @param valid
     */
    protected final void setValid(boolean valid){
        this.valid = valid;
    }

    /**
	 * Is this object dirty (ie some data need to be saved to device)
	 * @return
	 * @throws IOException
	 */
	public boolean isDirty() throws IOException 
	{
		return dirty;
	}
	
	/**
	 * Mark this object as dirty.
	 */
	final protected void setDirty() {
		this.dirty = true;
	}

	/**
	 * Mark this object as not dirty.
	 */
	final protected void resetDirty() {
		this.dirty = false;
	}

	/**
	 * Get the file system that this object belong to
	 */
	final public FileSystem getFileSystem() {
		return fileSystem;
	}
	
	/**
	 * Specify the rights on this object
	 * @param read
	 * @param write
	 */
	final public void setRights(boolean read, boolean write)
	{
		this.canRead = read;
		this.canWrite = write;
	}
	
	/**
	 * Can we read this object on device ?
	 * @return
	 */
	final public boolean canRead()
	{
		return this.canRead;
	}

	/**
	 * Can we write this object on device ?
	 * @return
	 */
	final public boolean canWrite()
	{
		return this.canWrite;
	}

	private boolean valid;
	private boolean dirty;
	
	// should use FSAccessRights for these fields
	private boolean canRead = true;
	private boolean canWrite = true;
	
	private AbstractFileSystem fileSystem;
}
