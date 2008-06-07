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
 
package org.jnode.fs.ntfs;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterator used to iterate over all IndexEntry's in an index block
 * or index_root attribute.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexEntryIterator implements Iterator<IndexEntry> { 
    private int offset;
    private IndexEntry nextEntry;
    private final NTFSStructure parent;
    private final FileRecord parentFileRecord;

    /**
     * Initialize this instance.
     */
    public IndexEntryIterator(FileRecord parentFileRecord, NTFSStructure parent, int firstOffset) {
        this.offset = firstOffset;
        this.parentFileRecord = parentFileRecord;
        this.parent = parent;
        readNext();
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (nextEntry == null) {
            return false;
        }
        return !nextEntry.isLastIndexEntryInSubnode() || nextEntry.hasSubNodes();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public IndexEntry next() { 
        if (nextEntry == null) { 
            throw new NoSuchElementException();
        } else {
            final IndexEntry result = nextEntry;
            final int size = nextEntry.getSize(); 
            offset += size;
            // Now read the next next entry
            readNext();
            return result;
        }
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void readNext() {
        nextEntry = new IndexEntry(parentFileRecord, parent, offset);
        if (nextEntry.isLastIndexEntryInSubnode() && !nextEntry.hasSubNodes()) {
            nextEntry = null;
        }
    }
}
