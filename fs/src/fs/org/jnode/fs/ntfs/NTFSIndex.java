/*
 * $Id$
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

    final static Logger log = Logger.getLogger(NTFSIndex.class);

    /**
     * Initialize this instance.
     * 
     * @param fileRecord
     */
    public NTFSIndex(FileRecord fileRecord) throws IOException {
        this.fileRecord = fileRecord;
        if (!fileRecord.isDirectory()) { throw new IOException(
                "fileRecord is not a directory"); }
    }

    /**
     * Gets the index root attribute.
     * 
     * @return
     */
    public IndexRootAttribute getIndexRootAttribute() {
        if (indexRootAttribute == null) {
            indexRootAttribute = (IndexRootAttribute) fileRecord
                    .getAttribute(NTFSAttribute.Types.INDEX_ROOT);
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
            indexAllocationAttribute = (IndexAllocationAttribute) fileRecord
                    .getAttribute(NTFSAttribute.Types.INDEX_ALLOCATION);
        }
        return indexAllocationAttribute;
    }

    public Iterator iterator() {
        log.debug("iterator");
        return new FullIndexEntryIterator();
    }

    class FullIndexEntryIterator implements Iterator {

        /**
         * List of those IndexEntry's that have a subnode and the subnode has
         * not been visited.
         */
        private final Queue subNodeEntries = new Queue();

        /** Iterator of current part of the index */
        private Iterator currentIterator;

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
        public Object next() {
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
                    nextEntry = (IndexEntry) currentIterator.next();
                    if (nextEntry.hasSubNodes()) {
                        log.debug("next has subnode");
                        subNodeEntries.add(nextEntry);
                    }
                    if (!nextEntry.isLastIndexEntryInSubnode()) { return; }
                }
                nextEntry = null;

                // Do we have subnodes to iterate over?
                if (subNodeEntries.isEmpty()) {
                    // No, we're done
                    log.debug("end of list");
                    return;
                }

                log.debug("hasNext: read next indexblock");
                final IndexEntry entry = (IndexEntry) subNodeEntries.get();
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