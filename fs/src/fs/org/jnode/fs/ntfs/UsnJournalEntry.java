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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An entry in the USN journal file ($Extend\$UsnJrnl).
 *
 * @author Luke Quinane
 */
public class UsnJournalEntry extends NTFSStructure {

    /**
     * Creates a new journal entry at the given offset.
     *
     * @param buffer the buffer containing the journal data.
     * @param offset the offset in the buffer to read from.
     */
    public UsnJournalEntry(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    /**
     * Gets the size of this entry.
     *
     * @return the size.
     */
    public long getSize() {
        return getUInt32(0x0);
    }

    /**
     * Gets the major version number.
     *
     * @return the major version number.
     */
    public int getMajorVersion() {
        return getUInt16(0x4);
    }

    /**
     * Gets the minor version number.
     *
     * @return the minor version number.
     */
    public int getMinorVersion() {
        return getUInt16(0x6);
    }

    /**
     * Gets the MFT reference.
     *
     * @return the MFT reference.
     */
    public long getMftReference() {
        return getInt48(0x8);
    }

    /**
     * Gets the parent MFT reference.
     *
     * @return the parent MFT reference.
     */
    public long getParentMtfReference() {
        return getInt48(0x10);
    }

    /**
     * Gets the timestamp for this entry.
     *
     * @return the timestamp.
     */
    public long getTimestamp() {
        return NTFSUTIL.filetimeToMillis(getInt64(0x20));
    }

    /**
     * Gets the reason for the entry.
     *
     * @return the reason.
     */
    public long getReason() {
        return getUInt32(0x28);
    }

    /**
     * Gets the source info.
     *
     * @return the source info.
     */
    public int getSourceInfo() {
        return getInt32(0x2b);
    }

    public int getSecurityId() {
        return getInt32(0x30);
    }

    public int getFileAttributes() {
        return getInt32(0x34);
    }

    /**
     * Gets the size of the file name stored in this entry.
     *
     * @return the size of the file name text.
     */
    public int getFileNameSize() {
        return getInt16(0x38);
    }

    /**
     * Gets the file name stored in this entry.
     *
     * @return the file name.
     */
    public String getFileName() {
        byte[] buffer = new byte[getFileNameSize()];
        getData(0x3c, buffer, 0, buffer.length);

        try {
            return new String(buffer, "UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-16LE charset missing from JRE", e);
        }
    }

    @Override
    public String toString() {
        return String.format("MFT: 0x%x parent MFT: 0x%x, %s version: %d.%d, size: %d source: 0x%x security: 0x%x "
            + "attributes: %s time: %s, name:%s", getMftReference(), getParentMtfReference(),
            Reason.lookupReasons(getReason()), getMajorVersion(), getMinorVersion(), getSize(), getSourceInfo(),
            getSecurityId(), FileAttribute.lookupAttributes(getFileAttributes()), new Date(getTimestamp()),
            getFileName());
    }

    /**
     * File attribute constants
     */
    public static class FileAttribute {
        /**
         * The lookup map for file attributes.
         */
        private static final Map<Long, String> attributeMap = new LinkedHashMap<Long, String>();

        /**
         * Registers a value in the map.
         *
         * @param value the value to register.
         * @param name  the name.
         * @return the value.
         */
        private static long register(long value, String name) {
            attributeMap.put(value, name);
            return value;
        }

        /**
         * Looks up the attributes for the given value.
         *
         * @param value the value to lookup.
         * @return the attributes or "unknown-xXYZ" if the value is not known.
         */
        public static List<String> lookupAttributes(long value) {
            List<String> reasons = new ArrayList<String>();

            for (Map.Entry<Long, String> entry : attributeMap.entrySet()) {
                if ((value & entry.getKey()) != 0) {
                    reasons.add(entry.getValue());
                    value -= entry.getKey();
                }
            }

            if (value != 0) {
                reasons.add("unknown-x" + Long.toHexString(value));
            }

            return reasons;
        }

        public static final long READONLY = register(0x1, "read-only");
        public static final long HIDDEN = register(0x2, "hidden");
        public static final long SYSTEM = register(0x4, "system");
        public static final long DIRECTORY = register(0x10, "directory");
        public static final long ARCHIVE = register(0x20, "archive");
        public static final long DEVICE = register(0x40, "device");
        public static final long NORMAL = register(0x80, "normal");
        public static final long TEMPORARY = register(0x100, "temporary");
        public static final long SPARSE = register(0x200, "sparse");
        public static final long REPARSE_POINT = register(0x400, "reparse-point");
        public static final long COMPRESSED = register(0x800, "compressed");
        public static final long OFFLINE = register(0x1000, "offline");
        public static final long NOT_INDEXED = register(0x2000, "not-indexed");
        public static final long VIRTUAL = register(0x10000, "virtual");

        public static final long ENCRYPTED = register(0x3FFF, "encrypted");
    }

    /**
     * Reason constants
     */
    public static class Reason {
        /**
         * The lookup map for reasons.
         */
        private static final Map<Long, String> reasonMap = new HashMap<Long, String>();

        /**
         * Registers a value in the map.
         *
         * @param value the value to register.
         * @param name  the name.
         * @return the value.
         */
        private static long register(long value, String name) {
            reasonMap.put(value, name);
            return value;
        }

        /**
         * Looks up the reasons for the given value.
         *
         * @param value the value to lookup.
         * @return the reasons or "unknown-xXYZ" if the value is not known.
         */
        public static List<String> lookupReasons(long value) {
            List<String> reasons = new ArrayList<String>();

            for (Map.Entry<Long, String> entry : reasonMap.entrySet()) {
                if ((value & entry.getKey()) != 0) {
                    reasons.add(entry.getValue());
                    value -= entry.getKey();
                }
            }

            if (value != 0) {
                reasons.add("unknown-x" + Long.toHexString(value));
            }

            return reasons;
        }

        /**
         * Data in one or more data streams was overwritten.
         */
        public static final long DATA_WRITE = register(0x1, "data-write");

        /**
         * File or directory added.
         */
        public static final long FS_ENTRY_ADDED = register(0x2, "fs-entry-added");

        /**
         * File or directory truncated.
         */
        public static final long FS_ENTRY_TRUNCATED = register(0x4, "fs-entry-truncated");

        /**
         * Data in one or more data streams was overwritten. Alternate value.
         */
        public static final long DATA_WRITE_ALT = register(0x10, "data-write-alt");

        /**
         * Data in one or more data streams was appended to.
         */
        public static final long DATA_APPEND = register(0x20, "data-append");

        /**
         * Data in one or more data streams was truncated.
         */
        public static final long DATA_TRUNCATED = register(0x40, "data-truncated");

        /**
         * File or directory created.
         */
        public static final long FS_ENTRY_CREATED = register(0x100, "fs-entry-created");

        /**
         * File or directory deleted.
         */
        public static final long FS_ENTRY_DELETED = register(0x200, "fs-entry-deleted");

        /**
         * User changed a file or directory's extended attributes.
         */
        public static final long FS_ENTRY_EX_ATTR_CHANGED = register(0x400, "fs-entry-ex-attr-changed");

        /**
         * File or directory access changes.
         */
        public static final long FS_ENTRY_ACCESS_CHANGED = register(0x800, "fs-entry-access-changed");

        /**
         * File or directory renamed. USN journal entry has the old name.
         */
        public static final long FS_ENTRY_RENAMED_OLD = register(0x1000, "fs-entry-rename-old");

        /**
         * File or directory renamed. USN journal entry has the new name.
         */
        public static final long FS_ENTRY_RENAMED_NEW = register(0x2000, "fs-entry-rename-new");

        /**
         * User changed whether the contents of a file was indexed or not.
         */
        public static final long FS_ENTRY_INDEXING_CHANGED = register(0x4000, "fs-entry-indexing-changed");

        /**
         * User changed a file or directories attributes or timestamps.
         */
        public static final long FS_ENTRY_ATTR_CHANGED = register(0x8000, "fs-entry-attr-changed");

        /**
         * A hard link was removed from a directory.
         */
        public static final long HARD_LINK_REMOVED = register(0x10000, "hard-link-removed");

        /**
         * User changed whether a file or directory is compressed.
         */
        public static final long FS_ENTRY_COMPRESSION_CHANGED = register(0x20000, "fs-entry-compression-changed");

        /**
         * User changed whether a file or directory is encrypted.
         */
        public static final long FS_ENTRY_ENCRYPTION_CHANGED = register(0x40000, "fs-entry-encryption-changed");

        /**
         * The user changed an object identifier for a file or directory.
         */
        public static final long FS_ENTRY_OBJ_IDENTIFIER_CHANGED = register(0x80000, "fs-entry-obj-identifier-changed");

        /**
         * A reparse point was altered, added or deleted in the directory.
         */
        public static final long REPARSE_POINT_ALTERED = register(0x100000, "reparse-point-altered");

        /**
         * A data stream for a file has been added, removed or renamed.
         */
        public static final long DATA_STREAM_ALTERED = register(0x200000, "data-stream-altered");

        /**
         * The file or directory was closed.
         */
        public static final long FS_ENTRY_CLOSED = register(0x80000000L, "fs-entry-closed");
    }

    /**
     * Source info constants
     */
    public static class SourceInfo {
        /**
         * The file or directory was changed by the operating system.
         */
        public static final int CHANGE_FROM_OS = 0x1;

        /**
         * Private stream data was added but the modifications did not change application data.
         */
        public static final int CHANGE_FROM_AUX = 0x2;

        /**
         * The change was related to the replication service.
         */
        public static final int REPLICATION = 0x4;
    }
}
