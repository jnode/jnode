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

import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.ntfs.NTFSStructure;

/**
 * An access control list as stored inside a {@link SecurityDescriptor}.
 *
 * @author Luke Quinane
 */
public class AccessControlList extends NTFSStructure {

    /**
     * The embedded ACEs.
     */
    private List<AccessControlEntry> aces;

    /**
     * Creates a new ACL at the given offset.
     *
     * @param parent the parent descriptor.
     * @param offset the offset to the ACL data.
     */
    public AccessControlList(SecurityDescriptor parent, int offset) {
        super(parent, offset);
    }

    /**
     * Gets the revision of the ACL.
     *
     * @return the revision.
     */
    public int getRevision() {
        return getInt8(0);
    }

    /**
     * Gets the size of the ACL.
     *
     * @return the size.
     */
    public int getSize() {
        return getInt16(2);
    }

    /**
     * Gets the number of ACE entries in the ACL.
     *
     * @return the number of entries.
     */
    public int getAceCount() {
        return getInt16(4);
    }

    /**
     * Gets the access control entries.
     *
     * @return the entries.
     */
    public List<AccessControlEntry> getAces() {
        if (aces == null) {
            aces = new ArrayList<AccessControlEntry>();
            int offset = 8;

            for (int i = 0; i < getAceCount(); i++) {
                AccessControlEntry entry = new AccessControlEntry(this, offset);
                aces.add(entry);
                offset += entry.getSize();
            }
        }

        return aces;
    }
}
