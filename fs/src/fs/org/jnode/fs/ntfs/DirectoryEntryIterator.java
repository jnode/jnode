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
 
package org.jnode.fs.ntfs;

import java.util.Iterator;

import org.jnode.fs.FSEntry;

/**
 * Iterator for FSEntry's.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DirectoryEntryIterator implements Iterator<FSEntry> {

    private final Iterator<IndexEntry> indexIterator;

    private final NTFSFileSystem fs;

    private NTFSEntry nextEntry;

    /**
     * Initialize this instance.
     * 
     * @param fs
     * @param index
     */
    public DirectoryEntryIterator(NTFSFileSystem fs, NTFSIndex index) {
        this.fs = fs;
        this.indexIterator = index.iterator();
        readNextEntry();
    }

    /**
     * Are there more entries.
     * 
     * @see org.jnode.fs.FSEntryIterator#hasNext()
     */
    public boolean hasNext() {
        return (nextEntry != null);
    }

    /**
     * Get the next entry.
     * 
     * @see org.jnode.fs.FSEntryIterator#next()
     */
    public FSEntry next() {
        final NTFSEntry result = nextEntry;
        readNextEntry();
        return result;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Read the next entry.
     */
    private final void readNextEntry() {
        while (true) {
            if (!indexIterator.hasNext()) {
                nextEntry = null;
                return;
            }

            final IndexEntry indexEntry = indexIterator.next();
            if (indexEntry.getNameSpace() != FileNameAttribute.NameSpace.DOS) {
                // Skip DOS filename entries.
                nextEntry = new NTFSEntry(fs, indexEntry);
                return;
            }
        }
    }
}
