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
 
package org.jnode.fs;

import java.io.IOException;

/**
 * @author epr
 */
/**
 * Entry of an FSDirectory.
 * 
 * @author epr
 */
public interface FSEntry extends FSObject {

    /**
     * Gets the name of this entry.
     */
    public String getName();

    /**
     * Gets the directory this entry is a part of.
     */
    public FSDirectory getParent();

    /**
     * Gets the last modification time of this entry.
     *
     * @return the last modification time of the entry as milliseconds since 1970, or {@code 0}
     *         if this filesystem does not support getting the last modified time.
     * @throws IOException if an error occurs retrieving the timestamp.
     */
    public long getLastModified() throws IOException;

    /**
     * Is this entry refering to a file?
     */
    public boolean isFile();

    /**
     * Is this entry refering to a (sub-)directory?
     */
    public boolean isDirectory();

    /**
     * Sets the name of this entry.
     */
    public void setName(String newName) throws IOException;

    /**
     * Gets the last modification time of this entry.
     * 
     * @throws IOException
     */
    public void setLastModified(long lastModified) throws IOException;

    /**
     * Gets the file this entry refers to. This method can only be called if
     * <code>isFile</code> returns true.
     * 
     * @return The file described by this entry
     */
    public FSFile getFile() throws IOException;

    /**
     * Gets the directory this entry refers to. This method can only be called
     * if <code>isDirectory</code> returns true.
     * 
     * @return The directory described by this entry
     */
    public FSDirectory getDirectory() throws IOException;

    /**
     * Gets the accessrights for this entry.
     * 
     * @throws IOException
     */
    public FSAccessRights getAccessRights() throws IOException;

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     * 
     * @return true if the entry need to be saved
     * @throws IOException
     */
    public boolean isDirty() throws IOException;
}
