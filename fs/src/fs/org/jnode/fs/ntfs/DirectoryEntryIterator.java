/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.util.Iterator;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;

/**
 * Iterator for FSEntry's.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DirectoryEntryIterator implements FSEntryIterator {

    private final Iterator indexIterator;

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
     * Read the next entry.
     */
    private final void readNextEntry() {
        while (true) {
            if (!indexIterator.hasNext()) {
                nextEntry = null;
                return;
            }

            final IndexEntry indexEntry = (IndexEntry) indexIterator.next();
            if (indexEntry.getNameSpace() != FileNameAttribute.NameSpace.DOS) {
                // Skip DOS filename entries.
                nextEntry = new NTFSEntry(fs, indexEntry);
                return;
            }
        }
    }
}