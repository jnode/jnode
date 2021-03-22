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

import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSStructure;

/**
 * Structure accessor of an Index Entry.
 * 
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IndexEntry extends NTFSStructure {

    /**
     * The offset to the index entry content.
     */
    public static final int CONTENT_OFFSET = 0x10;

    private final FileRecord parentFileRecord;

    /**
     * Initialize this instance.
     * @param parentFileRecord
     * @param buffer
     * @param offset
     */
    public IndexEntry(FileRecord parentFileRecord, byte[] buffer, int offset) {
        super(buffer, offset);
        this.parentFileRecord = parentFileRecord;
    }

    /**
     * Initialize this instance.
     * @param parentFileRecord
     * @param parent
     * @param offset
     */
    public IndexEntry(FileRecord parentFileRecord, NTFSStructure parent, int offset) {
        super(parent, offset);
        this.parentFileRecord = parentFileRecord;
    }

    public boolean hasSubNodes() {
        return (getIndexFlags() & 0x01) != 0;
    }

    /**
     * Gets the length of this index entry in bytes.
     * @return
     */
    public int getSize() {
        return getUInt16(0x08);
    }

    /**
     * Gets the content length of this index entry in bytes.
     * @return
     */
    public int getContentSize() {
        return getUInt16(0x0A);
    }

    /**
     * Gets the flags of this index entry.
     * @return
     */
    public int getIndexFlags() {
        return getUInt8(0x0C);
    }

    public boolean isLastIndexEntryInSubnode() {
        return (getIndexFlags() & 0x02) != 0;
    }

    public long getFileReferenceNumber() {
        return getUInt48(0x00);
    }

    /**
     * @return Returns the parentFileRecord.
     */
    public FileRecord getParentFileRecord() {
        return parentFileRecord;
    }

    public long getSubnodeVCN() {
        return getInt64(getSize() - 8); // TODO: getUInt64AsInt
        //return getUInt32(getSize() - 8);
    }

    @Override
    public String toString() {
        return String.format("index-entry:[flags: %d size:%d content-size:%d]", getIndexFlags(), getSize(),
            getContentSize());
    }
}
