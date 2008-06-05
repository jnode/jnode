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
 
package org.jnode.fs;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author epr
 */
public interface FSDirectory extends FSObject {

    /**
     * Gets an iterator used to iterate over all the entries of this directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    public Iterator<? extends FSEntry> iterator() throws IOException;

    /**
     * Gets the entry with the given name.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry getEntry(String name) throws IOException;

    /**
     * Add a new file with a given name to this directory.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry addFile(String name) throws IOException;

    /**
     * Add a new (sub-)directory with a given name to this directory.
     * 
     * @param name
     * @throws IOException
     */
    public FSEntry addDirectory(String name) throws IOException;

    /**
     * Remove the entry with the given name from this directory.
     * 
     * @param name
     * @throws IOException
     */
    public void remove(String name) throws IOException;

    /**
     * Save all dirty (unsaved) data to the device
     * 
     * @throws IOException
     */
    public void flush() throws IOException;

}
