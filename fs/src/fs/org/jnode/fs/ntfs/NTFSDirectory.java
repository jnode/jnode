/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;
import org.jnode.fs.FileSystem;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSDirectory implements FSDirectory {

    private static final Logger log = Logger.getLogger(NTFSDirectory.class);

    private final NTFSIndex index;

    private final NTFSFileSystem fs;

    /**
     * Initialize this instance.
     * 
     * @param record
     */
    public NTFSDirectory(NTFSFileSystem fs, FileRecord record)
            throws IOException {
        this.fs = fs;
        this.index = new NTFSIndex(record);
    }

    /**
     * Gets an iterator to iterate over all entries of this directory.
     */
    public FSEntryIterator iterator() {
        return new DirectoryEntryIterator(fs, index);
    }

    /**
     * Gets an entry with a given name.
     */
    public FSEntry getEntry(String name) {
        log.debug("getEntry(" + name + ")");
        for (FSEntryIterator it = this.iterator(); it.hasNext();) {
            final NTFSEntry entry = (NTFSEntry) it.next();
            if (entry.getName().equals(name)) { return entry; }
        }
        return null;
    }

    /**
     *  
     */
    public FSEntry addFile(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  
     */
    public FSEntry addDirectory(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Remove the entry with the given name from this directory.
     */
    public void remove(String name) {
        // TODO Auto-generated method stub

    }

    /**
     * Is this entry valid.
     */
    public boolean isValid() {
        return true;
    }

    /**
     *  
     */
    public FileSystem getFileSystem() {
        return fs;
    }

    /**
     * Save all dirty (unsaved) data to the device
     * 
     * @throws IOException
     */
    public void flush() throws IOException {
        //TODO
    }
}
