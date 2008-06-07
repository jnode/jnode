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

import java.io.IOException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MasterFileTable extends FileRecord {

    /** MFT indexes of system files */
    public static class SystemFiles {

        /**
         * Master file table (mft). Data attribute contains the entries and
         * bitmap attribute records which ones are in use (bit==1).
         */
        public static final int MFT = 0;

        /**
         * Mft mirror: copy of first four mft records in data attribute. If
         * cluster size > 4kiB, copy of first N mft records, with N =
         * cluster_size / mft_record_size.
         */
        public static final int MFTMIRR = 1;

        /**
         * Journalling log in data attribute.
         */
        public static final int LOGFILE = 2;

        /**
         * Volume name attribute and volume information attribute (flags and
         * ntfs version). Windows refers to this file as volume DASD (Direct
         * Access Storage Device).
         */
        public static final int VOLUME = 3;

        /**
         * Array of attribute definitions in data attribute.
         */
        public static final int ATTRDEF = 4;

        /** Root directory. */
        public static final int ROOT = 5;

        /**
         * Allocation bitmap of all clusters (lcns) in data attribute.
         */
        public static final int BITMAP = 6;

        /**
         * Boot sector (always at cluster 0) in data attribute.
         */
        public static final int BOOT = 7;

        /**
         * Contains all bad clusters in the non-resident data attribute.
         */
        public static final int BADCLUS = 8;

        /**
         * Shared security descriptors in data attribute and two indexes into
         * the descriptors. Appeared in Windows 2000. Before that, this file was
         * named $Quota but was unused.
         */
        public static final int SECURE = 9;

        /**
         * Uppercase equivalents of all 65536 Unicode characters in data
         * attribute.
         */
        public static final int UPCASE = 10;

        /**
         * Directory containing other system files (eg. $ObjId, $Quota, $Reparse
         * and $UsnJrnl). This is new to NTFS3.0.
         */
        public static final int EXTEND = 11;

        /** Reserved for future use (records 12-15). */
        public static final int RESERVED12 = 12;

        /** Reserved for future use (records 12-15). */
        public static final int RESERVED13 = 13;

        /** Reserved for future use (records 12-15). */
        public static final int RESERVED14 = 14;

        /** Reserved for future use (records 12-15). */
        public static final int RESERVED15 = 15;

        /**
         * First user file, used as test limit for whether to allow opening a
         * file or not.
         */
        public static final int FIRST_USER = 16;
    }

    /**
     * @param volume
     * @param buffer
     * @throws IOException
     */
    public MasterFileTable(NTFSVolume volume, byte[] buffer, int offset) throws IOException {
        super(volume, buffer, offset);
    }

    /**
     * Gets an MFT record with a given index.
     * 
     * @param index
     * @return
     */
    public FileRecord getRecord(long index) throws IOException {
        log.debug("getRecord(" + index + ")");

        final NTFSVolume volume = getVolume();
        final int bytesPerFileRecord = volume.getBootRecord().getFileRecordSize();
        final long offset = bytesPerFileRecord * index;

        // read the buffer
        final byte[] buffer = new byte[bytesPerFileRecord];
        readData(offset, buffer, 0, bytesPerFileRecord);
        return new FileRecord(volume, buffer, 0);
    }

    public FileRecord getIndexedFileRecord(IndexEntry indexEntry) throws IOException {
        return getRecord(indexEntry.getFileReferenceNumber());
    }
}
