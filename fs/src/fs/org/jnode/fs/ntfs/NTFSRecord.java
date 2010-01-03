/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
class NTFSRecord extends NTFSStructure {

    /** Size of an NTFS record in bytes */
    public static final int SIZE = 0x08;

    /** The volume this record is a part of */
    private final NTFSVolume volume;

    /** Magic constants */
    public static class Magic {
        /** Corrupt record */
        public static final int BAAD = 0x44414142;

        /** chkdsk ??? */
        public static final int CHKD = 0x424b4843;

        /** mft entry */
        public static final int FILE = 0x454c4946;

        /** ? (NTFS 3.0+?) */
        public static final int HOLE = 0x454c4f48;

        /** index buffer */
        public static final int INDX = 0x58444e49;
    }

    /**
     * @param buffer
     * @param offset
     */
    public NTFSRecord(NTFSVolume volume, byte[] buffer, int offset) throws IOException {
        super(buffer, offset);
        this.volume = volume;
        fixUp();
    }

    /**
     * @param parent
     * @param offset
     */
    public NTFSRecord(NTFSVolume volume, NTFSStructure parent, int offset) throws IOException {
        super(parent, offset);
        this.volume = volume;
        fixUp();
    }

    /**
     * Gets the magic value of this record.
     * 
     * @return
     */
    public int getMagic() {
        return getUInt32AsInt(0x00);
    }

    /**
     * Offset to the Update Sequence Array (usa) from the start of the ntfs
     * record.
     * 
     * @return
     */
    public int getUpdateSequenceArrayOffset() {
        return getUInt16(0x04);
    }

    /**
     * Number of u16 sized entries in the usa including the Update Sequence
     * Number (usn), thus the number of fixups is the usa_count minus 1.
     * 
     * @return
     */
    public int getUpdateSequenceArrayCount() {
        return getUInt16(0x06);
    }

    /** The volume this record is a part of */
    public NTFSVolume getVolume() {
        return volume;
    }

    /**
     * Perform the fixup of sector ends.
     */
    private void fixUp() throws IOException {
        final int updateSequenceOffset = getUpdateSequenceArrayOffset();
        final int usn = getUInt16(updateSequenceOffset);
        final int usnCount = getUpdateSequenceArrayCount();

        final BootRecord bootRecord = getVolume().getBootRecord();
        final int bytesPerSector = bootRecord.getBytesPerSector();

        // check each sector if the last 2 bytes are equal with the USN from
        // header

        for (int i = 1/* intended */; i < usnCount; i++) {
            final int bufOffset = (i * bytesPerSector) - 2;
            final int usnOffset = updateSequenceOffset + (i * 2);
            if (getUInt16(bufOffset) == usn) {
                setUInt16(bufOffset, getUInt16(usnOffset));
            } else {
                throw new IOException("Fixup error");
            }
        }
    }
}
