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


import org.jnode.fs.ntfs.NTFSStructure;

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public final class AttributeListEntry extends NTFSStructure {

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
     * Gets the length of the name.  Not so useful for callers, hence private.
     * @return the name length.
     */
    private int getNameLength() {
        return getUInt8(0x06);
    }

    /**
     * Gets the offset of the name.  Not so useful for callers, hence private.
     * @return the name offset (from the front of the entry.)
     */
    private int getNameOffset() {
        return getUInt8(0x07);
    }

    /**
     * Gets the starting VCN of the attribute, zero if the attribute is resident.
     * @return the starting VCN.
     */
    public int getStartingVCN() {
        return getUInt16(0x08);
    }

    /**
     * Gets the file reference number, which is the lowest 48 bits of the MFT
     * reference.  This may point to the same file record which contains the
     * attribute list. 
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
        return getUInt16(0x16);
    }

    /**
     * Gets the ID of the attribute.  This ID is unique within all attributes.
     * @return the attribute ID.
     */
    public int getAttributeID() {
        return getUInt16(0x18);
    }

    /**
     * Gets the name of the attribute.  Some attributes don't have names, and the names
     * on attributes are supposedly unique within a given attribute type.
     *
     * @return the name of the attribute referenced by this entry.  Returns the empty string
     *         if the attribute has no name.
     */
    public String getName() {
        final int nameLength = getNameLength();
        if (nameLength == 0) {
            return "";
        } else {
            char[] name = new char[nameLength];
            for (int i = 0, off = getNameOffset(); i < nameLength; i++, off += 2) {
                name[i] = getChar16(off);
            }
            return new String(name);
        }
    }

    @Override
    public String toString() {
        return String.format("attr-list-entry:[type=0x%x,name='%s',ref=%d,%s,id=0x%x]",
            getType(),
            getName(),
            getFileReferenceNumber(),
            getStartingVCN() == 0 ? "resident" : "vcn=" + getStartingVCN(),
            getAttributeID());
    }
}
