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

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public class StandardInformationAttribute extends NTFSResidentAttribute {

    /**
     * Constructs the attribute.
     *
     * @param fileRecord the containing file record.
     * @param offset offset of the attribute within the file record.
     */
    public StandardInformationAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time, as a 64-bit NTFS filetime value.
     */
    public long getCreationTime() {
        return getInt64(getAttributeOffset());
    }

    /**
     * Gets the modification time.
     *
     * @return the modification time, as a 64-bit NTFS filetime value.
     */
    public long getModificationTime() {
        return getInt64(getAttributeOffset() + 0x08);
    }

    /**
     * Gets the time when the MFT record last changed.
     *
     * @return the MFT change time, as a 64-bit NTFS filetime value.
     */
    public long getMftChangeTime() {
        return getInt64(getAttributeOffset() + 0x10);
    }

    /**
     * Gets the access time.
     *
     * @return the access time, as a 64-bit NTFS filetime value.
     */
    public long getAccessTime() {
        return getInt64(getAttributeOffset() + 0x18);
    }

    // TODO: The following fields have not yet been implemented due to no immediate need:
    //   offset  bytes  description
    //     0x20      4  Flags
    //     0x24      4  Maximum number of versions
    //     0x28      4  Version number
    //     0x2C      4  Class ID
    //     0x30      4  Owner ID (version 3.0+)
    //     0x34      4  Security ID (version 3.0+)
    //     0x38      8  Quota charged (version 3.0+)
    //     0x40      8  Update Sequence Number (USN) (version 3.0+)
}
