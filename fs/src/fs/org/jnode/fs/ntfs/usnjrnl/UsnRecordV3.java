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
 
package org.jnode.fs.ntfs.usnjrnl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.jnode.fs.ntfs.FileId128;
import org.jnode.fs.ntfs.NTFSStructure;
import org.jnode.fs.ntfs.NTFSUTIL;

/**
 * A v3 USN record entry in the USN journal file ($Extend\$UsnJrnl).
 *
 * @author Luke Quinane
 */
public class UsnRecordV3 extends NTFSStructure implements UsnRecordV2V3<FileId128> {

    /**
     * Creates a new journal entry at the given offset.
     *
     * @param buffer the buffer containing the journal data.
     * @param offset the offset in the buffer to read from.
     */
    public UsnRecordV3(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    @Override
    public long getSize() {
        return getUInt32(0x0);
    }

    @Override
    public int getMajorVersion() {
        return getUInt16(0x4);
    }

    @Override
    public int getMinorVersion() {
        return getUInt16(0x6);
    }

    @Override
    public FileId128 getMftReference() {
        return new FileId128(this, 0x8);
    }

    @Override
    public FileId128 getParentMtfReference() {
        return new FileId128(this, 0x18);
    }

    @Override
    public long getUsn() {
        return getInt64(0x20);
    }

    @Override
    public long getTimestamp() {
        return NTFSUTIL.filetimeToMillis(getInt64(0x28));
    }

    @Override
    public long getReason() {
        return getUInt32(0x30);
    }

    @Override
    public int getSourceInfo() {
        return getInt32(0x34);
    }

    public int getSecurityId() {
        return getInt32(0x38);
    }

    public int getFileAttributes() {
        return getInt32(0x3c);
    }

    @Override
    public int getFileNameSize() {
        return getInt16(0x40);
    }

    @Override
    public String getFileName() {
        byte[] buffer = new byte[getFileNameSize()];
        getData(0x44, buffer, 0, buffer.length);

        try {
            return new String(buffer, "UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-16LE charset missing from JRE", e);
        }
    }

    @Override
    public String toString() {
        return String.format("MFT: %s parent MFT: %s, %s version: %d.%d, size: %d source: 0x%x security: 0x%x "
            + "attributes: %s time: %s, name:%s", getMftReference(), getParentMtfReference(),
            UsnJournal.Reason.lookupReasons(getReason()), getMajorVersion(), getMinorVersion(), getSize(), getSourceInfo(),
            getSecurityId(), UsnJournal.FileAttribute.lookupAttributes(getFileAttributes()), new Date(getTimestamp()),
            getFileName());
    }
}
