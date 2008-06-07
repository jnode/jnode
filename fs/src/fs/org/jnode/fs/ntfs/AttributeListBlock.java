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

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure containing a list of {@link AttributeListEntry} entries.
 *
 * @author Daniel Noll (daniel@nuix.com.au)
 */
final class AttributeListBlock extends NTFSStructure {

    /**
     * The length of the block.
     */
    private long length;

    /**
     * @param data binary data for the block.
     * @param offset the offset into the binary data.
     */
    public AttributeListBlock(byte[] data, int offset, long length) {
        super(data, offset);
        this.length = length;
    }

    /**
     * Finds all entries from the attribute list with the given type ID.
     *
     * XXX: What if there are multiple?  In the case I've seen, there are
     *      multiple but only the first one contains any data.
     *
     * @param attrTypeID the type of attribute to find.
     * @return the attribute entry.
     */
    public List<AttributeListEntry> getEntries(int attrTypeID) {
        final List<AttributeListEntry> entries = new ArrayList<AttributeListEntry>();
        int offset = 0;
        while (offset + 4 <= length) // Should be just (offset < length) but it seems we have some uneven lengths.
        {
            try {
                int type = getUInt32AsInt(offset + 0x00);
                if (type == attrTypeID) {
                    entries.add(new AttributeListEntry(this, offset));
                }

                int length = getUInt16(offset + 0x04);
                offset += length;
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("...");
            }
        }
        return entries;
    }

}
