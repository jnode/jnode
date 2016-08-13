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
import org.jnode.fs.ntfs.attribute.NTFSResidentAttribute;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class FileNameAttribute extends NTFSResidentAttribute {

    public static class NameSpace {

        /**
         * This is the largest namespace. It is case sensitive and allows all Unicode characters except for: '\0' and
         * '/'. Beware that in WinNT/2k files which eg have the same name except for their case will not be
         * distinguished by the standard utilities and thus a "del filename" will delete both "filename" and "fileName"
         * without warning.
         */
        public static final int POSIX = 0x00;

        /**
         * The standard WinNT/2k NTFS long filenames. Case insensitive. All
         * Unicode chars except: '\0', '"', '*', '/', ':', ' <', '>', '?', '\'
         * and '|'. Further, names cannot end with a '.' or a space.
         */
        public static final int WIN32 = 0x01;

        /**
         * The standard DOS filenames (8.3 format). Uppercase only. All 8-bit
         * characters greater space, except: '"', '*', '+', ',', '/', ':', ';', '
         * <', '=', '>', '?' and '\'.
         */
        public static final int DOS = 0x02;

        /**
         * 3 means that both the Win32 and the DOS filenames are identical and hence have been saved in this single
         * filename record.
         */
        public static final int WIN32_AND_DOS = 0x03;
    }

    private final Structure fileNameStructure;

    /**
     * @param fileRecord
     * @param offset
     */
    public FileNameAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
        fileNameStructure = new Structure(this, getAttributeOffset());
    }

    /**
     * Gets the filename.
     *
     * @return
     */
    public String getFileName() {
        return fileNameStructure.getFileName();
    }

    /**
     * Is this a compressed file.
     *
     * @return
     */
    public boolean isCompressed() {
        return fileNameStructure.isCompressed();
    }

    /**
     * Gets the index of the parent MFT entry.
     *
     * @return the index of the parent MFT entry.
     */
    public long getParentMftIndex() {
        return fileNameStructure.getParentMftIndex();
    }

    /**
     * Gets the parent sequence number as recorded in the child record.
     *
     * @return the parent sequence number.
     */
    public int getParentSequenceNumber() {
        return fileNameStructure.getParentSequenceNumber();
    }

    /**
     * Gets the allocated file size.
     */
    public long getAllocatedFileSize() {
        return fileNameStructure.getAllocatedFileSize();
    }

    /**
     * Gets the real file size.
     */
    public long getRealSize() {
        return fileNameStructure.getRealSize();
    }

    /**
     * Gets the flags.
     */
    public int getFlags() {
        return fileNameStructure.getFlags();
    }

    /**
     * Gets the filename namespace.
     *
     * @return
     * @see NameSpace
     */
    public int getNameSpace() {
        return fileNameStructure.getNameSpace();
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time, as a 64-bit NTFS filetime value.
     */
    public long getCreationTime() {
        return fileNameStructure.getCreationTime();
    }

    /**
     * Gets the modification time.
     *
     * @return the modification time, as a 64-bit NTFS filetime value.
     */
    public long getModificationTime() {
        return fileNameStructure.getModificationTime();
    }

    /**
     * Gets the time when the MFT record last changed.
     *
     * @return the MFT change time, as a 64-bit NTFS filetime value.
     */
    public long getMftChangeTime() {
        return fileNameStructure.getMftChangeTime();
    }

    /**
     * Gets the access time.
     *
     * @return the access time, as a 64-bit NTFS filetime value.
     */
    public long getAccessTime() {
        return fileNameStructure.getAccessTime();
    }

    /**
     * The $FILE_NAME attribute structure. Also used in directory index records.
     */
    public static class Structure extends NTFSStructure {

        private String name;

        public Structure(NTFSStructure parent, int offset) {
            super(parent, offset);
        }

        /**
         * Gets the filename.
         *
         * @return
         */
        public String getFileName() {
            if (name == null) {
                try {
                    //XXX: For Java 6, should use the version that accepts a Charset.
                    name = new String(getFileNameAsByteArray(), "UTF-16LE");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("UTF-16LE charset missing from JRE", e);
                }
            }
            return name;
        }

        /**
         * Is this a compressed file.
         *
         * @return
         */
        public boolean isCompressed() {
            final int flags = getFlags();
            return ((flags & 0x0800) != 0);
        }

        /**
         * Checks whether this file name corresponds to a directory.
         *
         * @return {@code true} if a directory.
         */
        public boolean isDirectory() {
            return (getFlags() & 0x10000000L) != 0;
        }

        /**
         * Gets the index of the parent MFT entry.
         *
         * @return the index of the parent MFT entry.
         */
        public long getParentMftIndex() {
            return getInt48(0);
        }

        /**
         * Gets the parent sequence number as recorded in the child record.
         *
         * @return the parent sequence number.
         */
        public int getParentSequenceNumber() {
            return getUInt16(0x6);
        }

        /**
         * Gets the allocated file size.
         */
        public long getAllocatedFileSize() {
            return getInt64(0x28);
        }

        /**
         * Gets the real file size.
         */
        public long getRealSize() {
            return getInt64(0x30);
        }

        /**
         * Gets the flags.
         */
        public int getFlags() {
            return getUInt32AsInt(0x38);
        }

        /**
         * Gets the filename namespace.
         *
         * @return
         * @see NameSpace
         */
        public int getNameSpace() {
            return getUInt8(0x41);
        }

        /**
         * Gets the creation time.
         *
         * @return the creation time, as a 64-bit NTFS filetime value.
         */
        public long getCreationTime() {
            return getInt64(0x08);
        }

        /**
         * Gets the modification time.
         *
         * @return the modification time, as a 64-bit NTFS filetime value.
         */
        public long getModificationTime() {
            return getInt64(0x10);
        }

        /**
         * Gets the time when the MFT record last changed.
         *
         * @return the MFT change time, as a 64-bit NTFS filetime value.
         */
        public long getMftChangeTime() {
            return getInt64(0x18);
        }

        /**
         * Gets the access time.
         *
         * @return the access time, as a 64-bit NTFS filetime value.
         */
        public long getAccessTime() {
            return getInt64(0x20);
        }

        /**
         * Gets the name of this file as a byte array.
         *
         * @return the file name.
         */
        private byte[] getFileNameAsByteArray() {
            final int len = getUInt8(0x40);
            final byte[] bytes = new byte[len * 2];
            getData(0x42, bytes, 0, bytes.length);
            return bytes;
        }
    }
}
