/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
