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
 
package org.jnode.fs.ntfs.attribute;

import java.io.IOException;
import java.util.Iterator;
import org.jnode.fs.ntfs.FileRecord;

/**
 * $ATTRIBUTE_LIST attribute, non-resident version.
 * <p/>
 * XXX: Is there a sensible way we can merge this with the resident version?
 *
 * @author Daniel Noll (daniel@noll.id.au)
 */
public class AttributeListAttributeNonRes extends NTFSNonResidentAttribute implements
    AttributeListAttribute {

    /**
     * Creates a new attribute-list attribute.
     *
     * @param fileRecord the holding file record.
     * @param offset the offset to read from.
     */
    public AttributeListAttributeNonRes(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets an iterator over all the entries in the attribute list.
     *
     * @return an iterator of all attribute list entries.
     * @throws IOException if there is an error reading the attribute's data.
     */
    public Iterator<AttributeListEntry> getAllEntries() throws IOException {
        // Read the actual data from wherever it happens to be located.
        // TODO: Consider handling multiple data runs separately instead
        //       of "glueing" them all together like this.
        final int nrClusters = getDataRunDecoder().getNumberOfVCNs();
        log.debug(String.format("Allocating %d clusters for non-resident attribute", nrClusters));
        final byte[] data = new byte[nrClusters * getFileRecord().getClusterSize()];
        readVCN(getStartVCN(), data, 0, nrClusters);
        AttributeListBlock listBlock = new AttributeListBlock(data, 0, getAttributeActualSize());
        return listBlock.getAllEntries();
    }
}
