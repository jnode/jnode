/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

/**
 * $ATTRIBUTE_LIST attribute, resident version.
 *
 * XXX: Is there a sensible way we can merge this with the non-resident version?
 *
 * @author Daniel Noll (daniel@noll.id.au)
 */
final class AttributeListAttributeRes extends NTFSResidentAttribute implements
        AttributeListAttribute {

    /**
     * @param fileRecord
     * @param offset
     */
    public AttributeListAttributeRes(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets an iterator over all the entries in the attribute list.
     *
     * @return an iterator of all attribute list entries.
     * @throws IOException if there is an error reading the attribute's data.
     */
    public Iterator<AttributeListEntry> getAllEntries() throws IOException {
        final byte[] data = new byte[getAttributeLength()];
        getData(getAttributeOffset(), data, 0, data.length);
        AttributeListBlock listBlock = new AttributeListBlock(data, 0, getAttributeLength());
        return listBlock.getAllEntries();
    }

}
