/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexBlock extends NTFSRecord {

    /** Cached header instance */
    private IndexHeader header;

    /** Parent file */
    private final FileRecord parentFileRecord;

    /**
     * @param buffer
     * @param offset
     */
    public IndexBlock(FileRecord parentFileRecord, byte[] buffer, int offset) throws IOException {
        super(parentFileRecord.getVolume(), buffer, offset);
        this.parentFileRecord = parentFileRecord;
    }

    /**
     * $LogFile sequence number of the last modification of this index block.
     * 
     * @return
     */
    public long getLogFileSequenceNumber() {
        return getInt64(0x08);
    }

    /**
     * Virtual cluster number of the index block. If the cluster_size on the
     * volume is <= the index_block_size of the directory, index_block_vcn
     * counts in units of clusters, and in units of sectors otherwise.
     * 
     * @return
     */
    public long getIndexBlockVCN() {
        return getUInt32(0x10);
    }

    /**
     * Describes the following index entries.
     * 
     * @return
     */
    public IndexHeader getHeader() {
        if (header == null) {
            header = new IndexHeader(this, 0x18);
        }
        return header;
    }

    /**
     * Gets the parent file record.
     * 
     * @return
     */
    public FileRecord getParentFileRecord() {
        return parentFileRecord;
    }

    /**
     * Gets an iterator to iterate over all IndexEntry's in this block.
     * 
     * @return
     */
    public Iterator iterator() {
        return new IndexEntryIterator(parentFileRecord, this, getHeader()
                .getFirstEntryOffset() + 0x18);
    }
}