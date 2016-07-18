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
 
package org.jnode.fs.ntfs;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jnode.fs.ntfs.attribute.NTFSResidentAttribute;

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public class StandardInformationAttribute extends NTFSResidentAttribute {

    /**
     * Constructs the attribute.
     *
     * @param fileRecord the containing file record.
     * @param offset     offset of the attribute within the file record.
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

    /**
     * Gets the flags.
     *
     * @return the flags.
     */
    public int getFlags() {
        return getInt32(getAttributeOffset() + 0x20);
    }

    /**
     * Gets the maximum number of versions.
     *
     * @return the maximum.
     */
    public int getMaxVersions() {
        return getInt32(getAttributeOffset() + 0x24);
    }

    /**
     * Gets the version number.
     *
     * @return the version number.
     */
    public int getVersionNumber() {
        return getInt32(getAttributeOffset() + 0x28);
    }

    /**
     * Gets the class ID.
     *
     * @return the class ID.
     */
    public int getClassId() {
        return getInt32(getAttributeOffset() + 0x2c);
    }

    /**
     * Gets the owner ID (version 3.0+).
     *
     * @return the owner ID.
     */
    public int getOwnerId() {
        return getInt32(getAttributeOffset() + 0x30);
    }

    /**
     * Gets the security ID (version 3.0+).
     *
     * @return the security ID.
     */
    public int getSecurityId() {
        return getInt32(getAttributeOffset() + 0x34);
    }

    /**
     * Gets the quota charged (version 3.0+).
     *
     * @return the quota charged.
     */
    public int getQuotaCharged() {
        return getInt32(getAttributeOffset() + 0x38);
    }

    /**
     * Gets the update sequence number (USN) (version 3.0+).
     *
     * @return the update sequence number.
     */
    public int getUpdateSequenceNumber() {
        return getInt32(getAttributeOffset() + 0x40);
    }

    /**
     * The file attribute flags.
     */
    public static enum Flags {

        READ_ONLY("Read-only", 0x1),
        HIDDEN("Hidden", 0x2),
        SYSTEM("System", 0x4),
        ARCHIVE("Archive", 0x20),
        DEVICE("Archive", 0x40),
        NORMAL("Normal", 0x80),
        TEMPORARY("Temporary", 0x100),
        SPARSE("Sparse", 0x200),
        REPARSE_POINT("Reparse Point", 0x400),
        COMPRESSED("Compressed", 0x800),
        OFFLINE("Offline", 0x1000),
        NOT_INDEXED("Not Indexed", 0x2000),
        ENCRYPTED("Encrypted", 0x4000);

        /**
         * The name of the flag.
         */
        private final String name;

        /**
         * The value for the flag.
         */
        private final int value;

        /**
         * Creates a new instance.
         *
         * @param name  the name of the flag.
         * @param value the value for the flag.
         */
        Flags(String name, int value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Checks if the given value has this flag set.
         *
         * @param value the value to check.
         * @return {@code true} if the flag is set, {@code false} otherwise.
         */
        public boolean isSet(int value) {
            return (value & this.value) != 0;
        }

        /**
         * Gets a set of flag names that are set for the given value.
         *
         * @param value the value to decode.
         * @return the set of names.
         */
        public static Set<String> getNames(int value) {
            Set<String> names = new LinkedHashSet<String>();

            for (Flags flag : values()) {
                if (flag.isSet(value)) {
                    names.add(flag.name);
                    value -= flag.value;
                }
            }

            if (value != 0) {
                names.add(String.format("Unknown 0x%x", value));
            }

            return names;
        }
    }
}
