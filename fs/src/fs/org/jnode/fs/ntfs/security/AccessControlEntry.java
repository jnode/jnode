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
 * An access control entry as stored inside a {@link AccessControlList}.
 *
 * @author Luke Quinane
 */
public class AccessControlEntry extends NTFSStructure {

    /**
     * Creates a new ACE from the given ACL data.
     *
     * @param parent the parent ACL.
     * @param offset the offset to this ACE entry.
     */
    public AccessControlEntry(AccessControlList parent, int offset) {
        super(parent, offset);
    }

    /**
     * Gets the type for this entry.
     *
     * @return the type.
     */
    public int getType() {
        return getInt8(0);
    }

    /**
     * Gets the flags for this entry.
     *
     * @return the flags.
     */
    public int getFlags() {
        return getInt8(1);
    }

    /**
     * Gets the size of this entry.
     *
     * @return the size.
     */
    public int getSize() {
        return getInt16(2);
    }

    /**
     * Gets the access mask.
     *
     * @return the access mask.
     */
    public int getAccessMask() {
        return getInt32(4);
    }

    /**
     * Gets the SID associated with this entry.
     *
     * @return the SID.
     */
    public SecurityIdentifier getSid() {
        return SecurityUtils.readSid(this, 8);
    }

    /**
     * ACE types.
     */
    public static enum Type {
        ALLOW(0x00, "Allow"),
        DENY(0x01, "Deny"),
        AUDIT(0x02, "Audit");

        int type;
        String name;
            
        Type(int type, String name) {
            this.type = type;
            this.name = name;
        }
        
        public static List<String> namesForType(int type) {
            List<String> names = new ArrayList<String>();

            for (Type aceType : values()) {
                if ((aceType.type & type) == aceType.type) {
                    type -= aceType.type;
                    names.add(aceType.name);
                }
            }

            if (type != 0) {
                names.add(String.format("Unknown Type: 0x%x", type));
            }

            return names;
        }
    }

    /**
     * ACE flags.
     */
    public static enum Flags {
        OBJECT_INHERITS(0x01, "Object Inherits"),
        CONTAINER_INHERITS(0x02, "Container Inherits"),
        DONT_PROPAGATE_INHERIT_ACE(0x04, "No Propagate Inherit ACE"),
        INHERIT_ONLY_ACE(0x08, "Inherit Only ACE"),

        AUDIT_SUCCESS(0x40, "Audit Success"),
        AUDIT_FAILURE(0x80, "Audit Failure");

        int flags;
        String name;
            
        Flags(int flags, String name) {
            this.flags = flags;
            this.name = name;
        }

        /**
         * Gets a list of matching flags names for the given value.
         *
         * @param flags the flags to look up.
         * @return the list of names.
         */
        public static List<String> namesForFlags(int flags) {
            List<String> names = new ArrayList<String>();

            for (Flags aceFlags : values()) {
                if ((aceFlags.flags & flags) == aceFlags.flags) {
                    flags -= aceFlags.flags;
                    names.add(aceFlags.name);
                }
            }

            if (flags != 0) {
                names.add(String.format("Unknown Flags: 0x%x", flags));
            }

            return names;
        }
    }

    /**
     * ACE mask.
     */
    public static enum Mask {
        READ(0x01, "Read", "List Contents"),
        WRITE(0x02, "Write", "Add File"),
        APPEND(0x04, "Write", "Add Subdirectory"),
        READ_EXTENDED_ATTRIBUTES(0x08, "Read Extended Attributes"),
        WRITE_EXTENDED_ATTRIBUTES(0x10, "Write Extended Attributes"),
        EXECUTE(0x20, "Execute File", "Traverse Directory"),
        DELETE_CHILD(0x40, "Delete Child"),
        READ_ATTRIBUTES(0x80, "Read Attributes"),
        WRITE_ATTRIBUTES(0x100, "Write Attributes"),
        DELETE(0x10000, "Delete"),
        READ_CONTROL(0x20000, "Read Security Descriptor"),
        WRITE_DAC(0x40000, "Write DACL"),
        WRITE_OWNER(0x80000, "Write Owner"),
        SYNCHRONIZE(0x100000, "Synchronize");

        /**
         * The mask value.
         */
        int mask;

        /**
         * The name of the associated file permission.
         */
        String filePermission;

        /**
         * The name of the associated directory permission.
         */
        String directoryPermission;

        Mask(int mask, String permission) {
            this(mask, permission, permission);
        }

        Mask(int mask, String filePermission, String directoryPermission) {
            this.mask = mask;
            this.filePermission = filePermission;
            this.directoryPermission = directoryPermission;
        }

        /**
         * Gets a list of matching permission names for the given mask value.
         *
         * @param mask the mask.
         * @param isDirectory {@code true} to look up the names for a directory, {@code false} to get the names for a
         *   file.
         * @return the list of names.
         */
        public static List<String> namesForMask(int mask, boolean isDirectory) {
            List<String> names = new ArrayList<String>();

            for (Mask aceMask : values()) {
                if ((aceMask.mask & mask) == aceMask.mask) {
                    mask -= aceMask.mask;
                    names.add(isDirectory ? aceMask.directoryPermission : aceMask.filePermission);
                }
            }

            if (mask != 0) {
                names.add(String.format("Unknown Access Mask: 0x%x", mask));
            }

            return names;
        }
    }
}
