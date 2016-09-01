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
 * A security descriptor.
 *
 * @author Luke Quinane
 */
public class SecurityDescriptor extends NTFSStructure {

    /**
     * Creates a new security descriptor from the given offset in the buffer.
     *
     * @param buffer the buffer to read from.
     * @param offset the offset to read at.
     */
    public SecurityDescriptor(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    /**
     * Gets the revision.
     *
     * @return the revision.
     */
    public int getRevision() {
        return getInt8(0);
    }

    /**
     * Gets the size (only useful if the resource manager control bit is set).
     *
     * @return the size.
     */
    public int getSize() {
        return getInt8(1);
    }

    /**
     * Gets the control flags.
     *
     * @return the control flags.
     */
    public int getFlags() {
        return getInt16(2);
    }

    /**
     * Gets the owner SID.
     *
     * @return the owner SID or {@code null} if the owner is not set.
     */
    public SecurityIdentifier getOwnerSid() {
        return getSidFromOffsetReference(4);
    }

    /**
     * Gets the group SID.
     *
     * @return the group SID or {@code null} if the group is not set.
     */
    public SecurityIdentifier getGroupSid() {
        return getSidFromOffsetReference(8);
    }

    /**
     * Reads in a SID from a offset referenced at the given offset in the buffer. E.g. at offset 4 in the structure read
     * the offset to the owner SID and then read the SID.
     *
     * @param offset the offset to read the reference from.
     * @return the SID or {@code null} if the SID was not set.
     */
    private SecurityIdentifier getSidFromOffsetReference(int offset) {
        int sidOffset = getInt32(offset);

        if (sidOffset == 0) {
            // SID not set
            return null;
        }

        return SecurityUtils.readSid(this, sidOffset);
    }

    /**
     * Gets the discretionary access control list (ACL). This specifies which users and groups have access to the item.
     *
     * @return the ACL or {@code null} if the ACL is not set.
     */
    public AccessControlList getDiscretionaryAcl() {
        int aclOffset = getInt32(0xc);

        if (aclOffset == 0) {
            // ACL not set
            return null;
        }

        return new AccessControlList(this, aclOffset);
    }

    /**
     * Gets the discretionary access control list (ACL). This controls the generation of audit messages for attempts to
     * access the object.
     *
     * @return the ACL or {@code null} if the ACL is not set.
     */
    public AccessControlList getSystemAcl() {
        int aclOffset = getInt32(0x10);

        if (aclOffset == 0) {
            // ACL not set
            return null;
        }

        return new AccessControlList(this, aclOffset);
    }
}
