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
 
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.util.Date;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class ISO9660Entry implements FSEntry {

    private ISO9660FileSystem fs;
    private EntryRecord entryRecord = null;

    public ISO9660Entry(ISO9660FileSystem fs, EntryRecord entry) {
        this.fs = fs;
        this.entryRecord = entry;
    }

    /**
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        return entryRecord.getFileIdentifier();
    }

    /**
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public FSDirectory getParent() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSEntry#getLastModified()
     */
    public long getLastModified() throws IOException {
        // FIXME Correct implementation to return real last modified time for
        // ISO9660 entry.
        Date now = new Date();
        return now.getTime();
    }

    /**
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        return !entryRecord.isDirectory();
    }

    /**
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        return entryRecord.isDirectory();
    }

    /**
     * @see org.jnode.fs.FSEntry#setName(java.lang.String)
     */
    public void setName(String newName) throws IOException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSEntry#setLastModified(long)
     */
    public void setLastModified(long lastModified) throws IOException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public FSFile getFile() throws IOException {
        return new ISO9660File(this);
    }

    /**
     * @see org.jnode.fs.FSEntry#getDirectory()
     */
    public FSDirectory getDirectory() throws IOException {
        return new ISO9660Directory(this);
    }

    /**
     * @see org.jnode.fs.FSEntry#getAccessRights()
     */
    public FSAccessRights getAccessRights() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem getFileSystem() {
        return fs;
    }

    /**
     * @return Returns the cDFSentry.
     */
    public EntryRecord getCDFSentry() {
        return entryRecord;
    }

    /**
     * @param sentry The cDFSentry to set.
     */
    public void setCDFSentry(EntryRecord sentry) {
        entryRecord = sentry;
    }

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     * 
     * @return true if the entry need to be saved
     * @throws IOException
     */
    public boolean isDirty() throws IOException {
        return true;
    }
}
