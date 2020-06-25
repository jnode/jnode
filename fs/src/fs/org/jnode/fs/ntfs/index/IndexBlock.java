/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.ntfs.index;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSRecord;

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
        super(true, buffer, offset);
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
    public Iterator<IndexEntry> iterator() {
        return new IndexEntryIterator(parentFileRecord, this,
                getHeader().getFirstEntryOffset() + 0x18);
    }
}
