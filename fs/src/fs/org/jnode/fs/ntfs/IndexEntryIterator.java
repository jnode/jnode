/*
 * $Id$
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
final class IndexEntryIterator implements Iterator {
    
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
    public Object next() {
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