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
import java.util.NoSuchElementException;


/**
 * Iterator used to iterate over all IndexEntry's in an index block
 * or index_root attribute.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexEntryIterator implements Iterator<IndexEntry> {
    
    //private final Logger log = Logger.getLogger(getClass());
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
        //log.debug("next: offset=" + offset);
        if (nextEntry == null) {
            //log.debug("next: islast");
            throw new NoSuchElementException();
        } else {
            final IndexEntry result = nextEntry;
            final int size = nextEntry.getSize();
            //log.debug("next: size=" + size);
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
