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

import org.jnode.fs.ntfs.FileRecord;

/**
 * An NTFS file attribute that has its data stored inside the attribute.
 *
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSResidentAttribute extends NTFSAttribute {

    /**
     * @param fileRecord
     * @param offset
     */
    public NTFSResidentAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the offset to the actual attribute.
     *
     * @return Returns the attributeOffset.
     */
    public int getAttributeOffset() {
        return getUInt16(0x14);
    }

    /**
     * @return Returns the indexedFlag.
     */
    public int getIndexedFlag() {
        return getUInt8(0x16);
    }

    public int getAttributeLength() {
        return (int) getUInt32(0x10);
    }

    @Override
    public String toString() {
        return String.format("[attribute (res) type=x%x name'%s' size=%d]", getAttributeType(), getAttributeName(),
            getAttributeLength());
    }

    @Override
    public String toDebugString() {
        return toString() + " data:" + hexDump();
    }
}
