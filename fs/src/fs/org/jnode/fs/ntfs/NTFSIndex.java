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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.util.Queue;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class NTFSIndex {

    private final FileRecord fileRecord;

    private IndexRootAttribute indexRootAttribute;

    private IndexAllocationAttribute indexAllocationAttribute;

    static final Logger log = Logger.getLogger(NTFSIndex.class);

    /**
     * Initialize this instance.
     * 
     * @param fileRecord
     */
    public NTFSIndex(FileRecord fileRecord) throws IOException {
        this.fileRecord = fileRecord;
        if (!fileRecord.isDirectory()) {
            throw new IOException("fileRecord is not a directory");
        }
    }

    /**
     * Gets the index root attribute.
     * 
     * @return
     */
    public IndexRootAttribute getIndexRootAttribute() {
        if (indexRootAttribute == null) {
            indexRootAttribute = (IndexRootAttribute) 
                    fileRecord.findAttributeByType(NTFSAttribute.Types.INDEX_ROOT);
            log.debug("getIndexRootAttribute: " + indexRootAttribute);
        }
        return indexRootAttribute;
    }

    /**
     * Gets the index allocation attribute, if any.
     * 
     * @return
     */
    public IndexAllocationAttribute getIndexAllocationAttribute() {
        if (indexAllocationAttribute == null) {
            indexAllocationAttribute = (IndexAllocationAttribute) 
                    fileRecord.findAttributeByType(NTFSAttribute.Types.INDEX_ALLOCATION);
        }
        return indexAllocationAttribute;
    }

    public Iterator<IndexEntry> iterator() {
        log.debug("iterator");
        return new FullIndexEntryIterator();
    }

    class FullIndexEntryIterator implements Iterator<IndexEntry> {

        /**
         * List of those IndexEntry's that have a subnode and the subnode has
         * not been visited.
         */
        private final Queue<IndexEntry> subNodeEntries = new Queue<IndexEntry>();

        /** Iterator of current part of the index */
        private Iterator<IndexEntry> currentIterator;

        private IndexEntry nextEntry;

        /**
         * Initialize this instance.
         */
        public FullIndexEntryIterator() {
            log.debug("FullIndexEntryIterator");
            currentIterator = getIndexRootAttribute().iterator();
            log.debug("currentIterator=" + currentIterator);
            readNextEntry();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return (nextEntry != null);
        }

        /**
         * @see java.util.Iterator#next()
         */
        public IndexEntry next() {
            final IndexEntry result = nextEntry;
            if (result == null) {
                throw new NoSuchElementException();
            }
            readNextEntry();
            return result;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void readNextEntry() {
            while (true) {
                if (currentIterator.hasNext()) {
                    // Read it
                    nextEntry = currentIterator.next();
                    if (nextEntry.hasSubNodes()) {
                        log.debug("next has subnode");
                        subNodeEntries.add(nextEntry);
                    }
                    if (!nextEntry.isLastIndexEntryInSubnode()) {
                        return;
                    }
                }
                nextEntry = null;

                // Do we have subnodes to iterate over?
                if (subNodeEntries.isEmpty()) {
                    // No, we're done
                    log.debug("end of list");
                    return;
                }

                log.debug("hasNext: read next indexblock");
                final IndexEntry entry = subNodeEntries.get();
                final IndexRoot indexRoot = getIndexRootAttribute().getRoot();
                final IndexBlock indexBlock;
                try {
                    indexBlock = getIndexAllocationAttribute().getIndexBlock(
                            indexRoot, entry.getSubnodeVCN());
                } catch (IOException ex) {
                    log.error("Cannot read next index block", ex);
                    return;
                }
                currentIterator = indexBlock.iterator();
            }
        }
    }
}
