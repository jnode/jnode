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


/**
 * @author Daniel Noll (daniel@nuix.com.au)
 */
final class AttributeListEntry extends NTFSStructure {

    /**
     * Creates the entry.
     * 
     * @param block the containing attribute list block.
     * @param offset the offset of this attribute within the block.
     */
    public AttributeListEntry(AttributeListBlock block, int offset) {
        super(block, offset);
    }

    /**
     * Gets the type of the attribute.
     * 
     * @return the type of the attribute.
     */
    public int getType() {
        return getUInt32AsInt(0x00);
    }

    /**
     * Gets the size of the attribute, in bytes.
     * 
     * @return the size of the attribute, in bytes.
     */
    public int getSize() {
        return getUInt16(0x04);
    }

    /**
     * Gets the file reference number, which is the lowest 48 bits of the MFT
     * reference.
     * 
     * @return the file reference number.
     */
    public long getFileReferenceNumber() {
        return getUInt48(0x10);
    }

    /**
     * Gets the file sequence number, which is the highest 16 bits of the MFT
     * reference.
     * 
     * @return the file sequence number.
     */
    public long getFileSequenceNumber() {
        return getUInt48(0x16);
    }

    // TODO:
    // 0x06 1 Name length (N)
    // 0x07 1 Offset to Name (a)
    // 0x08 8 Starting VCN (b)
    // 0x18 2 Attribute Id (c)
    // 0x1A 2N Name in Unicode (if N >0)
}
