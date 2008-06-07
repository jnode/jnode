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
 
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EntryRecord extends Descriptor {

    private final int entryLength;
    private final int extAttributeLength;
    private final long extentLocation;
    private final int dataLength;
    private final int fileUnitSize;
    private final int flags;
    private final int interleaveSize;
    private final String identifier;

    private final ISO9660Volume volume;
    private final String encoding;

    /**
     * Initialize this instance.
     * @param volume
     * @param buff
     * @param bp
     */
    public EntryRecord(ISO9660Volume volume, byte[] buff, int bp, String encoding) {
        final int offset = bp - 1;
        this.volume = volume;
        this.encoding = encoding;
        this.entryLength = getUInt8(buff, offset + 1);
        this.extAttributeLength = getUInt8(buff, offset + 2);
        this.extentLocation = getUInt32LE(buff, offset + 3);
        this.dataLength = (int) getUInt32LE(buff, offset + 11);
        this.fileUnitSize = getUInt8(buff, offset + 27);
        this.interleaveSize = getUInt8(buff, offset + 28);
        this.flags = getUInt8(buff, offset + 26);
        // This must be after flags, because of isDirectory.
        this.identifier = getFileIdentifier(buff, offset, isDirectory(), encoding);
    }

    public void readFileData(long offset, byte[] buffer, int bufferOffset, int size)
        throws IOException {
        volume.readFromLBN(this.getLocationOfExtent(), offset, buffer, bufferOffset, size);
    }

    public byte[] getExtentData() throws IOException {
        byte[] buffer = new byte[this.getDataLength()];
        volume.readFromLBN(this.getLocationOfExtent(), 0, buffer, 0, this.getDataLength());
        return buffer;
    }

    public int getLengthOfDirectoryEntry() {
        return entryLength;
    }

    public int getLengthOfExtendedAttribute() {
        return extAttributeLength;
    }

    public long getLocationOfExtent() {
        return extentLocation;
    }

    public int getDataLength() {
        return dataLength;
    }

    public final boolean isDirectory() {
        return (flags & 0x03) != 0;
    }

    public final boolean isLastEntry() {
        return (flags & 0x40) == 0;
    }

    public final int getFlags() {
        return flags;
    }

    public final int getFileUnitSize() {
        return fileUnitSize;
    }

    public final int getInterleaveSize() {
        return interleaveSize;
    }

    public String getFileIdentifier() {
        return identifier;
    }

    /**
     * @return Returns the volume.
     */
    public final ISO9660Volume getVolume() {
        return volume;
    }

    private final String getFileIdentifier(byte[] buff, int offset, boolean isDir, String encoding) {
        final int fidLength = getUInt8(buff, offset + 33);
        if (isDir) {
            final int buff34 = getUInt8(buff, offset + 34);
            if ((fidLength == 1) && (buff34 == 0x00)) {
                return ".";
            } else if ((fidLength == 1) && (buff34 == 0x01)) {
                return "..";
            }
        }
        try {
            final String id = getDChars(buff, offset + 34, fidLength, encoding);
            final int sep2Idx = id.indexOf(SEPARATOR2);
            if (sep2Idx >= 0) {
                return id.substring(0, sep2Idx);
            } else {
                return id;
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return Returns the encoding.
     */
    public final String getEncoding() {
        return this.encoding;
    }
}
