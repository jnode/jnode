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
 
package org.jnode.fs.ntfs.security;

import org.jnode.fs.ntfs.NTFSStructure;

/**
 * A security descriptor entry located in the combined '$Secure:$SDS' stream.
 *
 * @author Luke Quinane
 */
public class SecurityDescriptorStreamEntry extends NTFSStructure {

    /**
     * The security descriptor.
     */
    private final SecurityDescriptor securityDescriptor;

    /**
     * Creates a new stream entry.
     *
     * @param buffer the buffer to read from.
     */
    public SecurityDescriptorStreamEntry(byte[] buffer) {
        super(buffer, 0);

        securityDescriptor = new SecurityDescriptor(buffer, 0x14);
    }

    /**
     * Gets the hash for the security descriptor entry.
     *
     * @return the hash of the entry.
     */
    public int getEntryHash() {
        return getInt32(0);
    }

    /**
     * Gets the security ID for the security descriptor entry. Note this is different to a security identifier or 'SID'.
     * This is the key that links the file on disk to the security descriptor containing SIDs.
     *
     * @return the security ID.
     */
    public int getSecurityId() {
        return getInt32(4);
    }

    /**
     * Gets the offset to this entry in the file or stream.
     *
     * @return the offset.
     */
    public int getOffsetToEntry() {
        return getInt32(8);
    }

    /**
     * Gets the associated security descriptor.
     *
     * @return the descriptor.
     */
    public SecurityDescriptor getSecurityDescriptor() {
        return securityDescriptor;
    }

    /**
     * Gets the length of this entry.
     *
     * @return the length.
     */
    public int getLength() {
        int length = getInt32(0x10);

        // Round up to a 16-byte boundary
        long rounding = length % 16 == 0 ? 0 : 16 - length % 16;
        length += rounding;

        return length;
    }
}
