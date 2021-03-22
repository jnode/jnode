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

import java.io.IOException;
import org.jnode.fs.ntfs.attribute.NTFSAttribute;
import org.jnode.fs.ntfs.index.IndexEntry;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MasterFileTable extends FileRecord {

    /**
     * MFT indexes of system files
     */
    public static class SystemFiles {

        /**
         * Master file table (mft). Data attribute contains the entries and bitmap attribute records which ones are in
         * use (bit==1).
         */
        public static final int MFT = 0;

        /**
         * Mft mirror: copy of first four mft records in data attribute. If cluster size > 4kiB, copy of first N mft
         * records, with N = cluster_size / mft_record_size.
         */
        public static final int MFTMIRR = 1;

        /**
         * Journalling log in data attribute.
         */
        public static final int LOGFILE = 2;

        /**
         * Volume name attribute and volume information attribute (flags and ntfs version). Windows refers to this file
         * as volume DASD (Direct Access Storage Device).
         */
        public static final int VOLUME = 3;

        /**
         * Array of attribute definitions in data attribute.
         */
        public static final int ATTRDEF = 4;

        /**
         * Root directory.
         */
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
         * Shared security descriptors in data attribute and two indexes into the descriptors. Appeared in Windows 2000.
         * Before that, this file was named $Quota but was unused.
         */
        public static final int SECURE = 9;

        /**
         * Uppercase equivalents of all 65536 Unicode characters in data attribute.
         */
        public static final int UPCASE = 10;

        /**
         * Directory containing other system files (eg. $ObjId, $Quota, $Reparse and $UsnJrnl). This is new to NTFS3.0.
         */
        public static final int EXTEND = 11;

        /**
         * Reserved for future use (records 12-15).
         */
        public static final int RESERVED12 = 12;

        /**
         * Reserved for future use (records 12-15).
         */
        public static final int RESERVED13 = 13;

        /**
         * Reserved for future use (records 12-15).
         */
        public static final int RESERVED14 = 14;

        /**
         * Reserved for future use (records 12-15).
         */
        public static final int RESERVED15 = 15;

        /**
         * First user file, used as test limit for whether to allow opening a file or not.
         */
        public static final int FIRST_USER = 16;
    }

    /**
     * The cached length of the MFT.
     */
    private long mftLength;

    /**
     * @param volume
     * @param buffer
     * @throws IOException
     */
    public MasterFileTable(NTFSVolume volume, byte[] buffer, int offset) throws IOException {
        super(volume, SystemFiles.MFT, buffer, offset);
    }

    /**
     * Creates a new MFT instance.
     *
     * @param volume the NTFS volume.
     * @param clusterSize the cluster size.
     * @param strictFixUp indicates whether to throw an exception if a fix-up error is detected.
     * @param buffer the buffer to read from.
     * @param offset the offset to read at.
     * @throws IOException if an error occurs creating the MFT.
     */
    public MasterFileTable(NTFSVolume volume, int clusterSize, boolean strictFixUp, byte[] buffer, int offset) throws IOException {
        super(volume, clusterSize, strictFixUp, SystemFiles.MFT, buffer, offset);
    }

    /**
     * Gets the length of the MFT.
     *
     * @return the length.
     */
    public long getMftLength() {
        if (mftLength == 0) {
            // The MFT doesn't update the FileRecord file-size for itself, so fall back to check the size of the DATA
            // attribute.
            mftLength = getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
        }

        return mftLength;
    }

    /**
     * Reads the bytes for a MFT record with a given index but does not check if it is a valid file record.
     *
     * @param index the index to get.
     * @return the file record.
     */
    public byte[] readRecord(long index) throws IOException {
        final NTFSVolume volume = getVolume();
        final int bytesPerFileRecord = volume.getBootRecord().getFileRecordSize();
        final long offset = bytesPerFileRecord * index;

        // read the buffer
        final byte[] buffer = new byte[bytesPerFileRecord];
        readData(offset, buffer, 0, bytesPerFileRecord);
        return buffer;
    }

    /**
     * Gets a MFT record with a given index but does not check if it is a valid file record.
     *
     * @param index the index to get.
     * @return the file record.
     */
    public FileRecord getRecordUnchecked(long index) throws IOException {
        log.debug("getRecord(" + index + ")");

        final NTFSVolume volume = getVolume();

        // read the buffer
        final byte[] buffer = readRecord(index);
        return new FileRecord(volume, index, buffer, 0);
    }

    /**
     * Gets a MFT record with a given index.
     *
     * @param index the index to get.
     * @return the file record.
     * @throws IOException if the record at the index is not valid or there is an error reading in the data.
     */
    public FileRecord getRecord(long index) throws IOException {
        final NTFSVolume volume = getVolume();
        final int bytesPerFileRecord = volume.getBootRecord().getFileRecordSize();
        final long offset = bytesPerFileRecord * index;

        if (offset + bytesPerFileRecord > getMftLength()) {
            throw new IOException("Attempt to read past the end of the MFT, offset: " + offset);
        }

        FileRecord fileRecord = getRecordUnchecked(index);
        fileRecord.checkIfValid();
        return fileRecord;
    }

    public FileRecord getIndexedFileRecord(IndexEntry indexEntry) throws IOException {
        return getRecord(indexEntry.getFileReferenceNumber());
    }
}
