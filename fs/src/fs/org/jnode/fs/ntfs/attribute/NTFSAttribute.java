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
 
package org.jnode.fs.ntfs.attribute;

import org.jnode.fs.ntfs.FileNameAttribute;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSStructure;
import org.jnode.fs.ntfs.StandardInformationAttribute;
import org.jnode.fs.ntfs.index.IndexAllocationAttribute;
import org.jnode.fs.ntfs.index.IndexRootAttribute;
import org.jnode.fs.util.FSUtils;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class NTFSAttribute extends NTFSStructure {

    public static final class Types {

        public static final int STANDARD_INFORMATION = 0x10;

        public static final int ATTRIBUTE_LIST = 0x20;

        public static final int FILE_NAME = 0x30;

        public static final int VOLUME_VERSION = 0x40;

        public static final int OBJECT_ID = 0x40;

        public static final int SECURITY_DESCRIPTOR = 0x50;

        public static final int VOLUME_NAME = 0x60;

        public static final int VOLUME_INFORMATION = 0x70;

        public static final int DATA = 0x80;

        public static final int INDEX_ROOT = 0x90;

        public static final int INDEX_ALLOCATION = 0xA0;

        public static final int BITMAP = 0xB0;

        public static final int SYMBOLIC_LINK = 0xC0;

        public static final int REPARSE_POINT = 0xC0;

        public static final int EA_INFORMATION = 0xD0;

        public static final int EA = 0xE0;

        public static final int PROPERTY_SET = 0xF0;

        public static final int LOGGED_UTILITY_STREAM = 0x100;
    }

    private final int type;

    private final int flags;

    private final FileRecord fileRecord;

    /**
     * Initialize this instance.
     */
    public NTFSAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
        this.fileRecord = fileRecord;
        this.type = getUInt32AsInt(0);
        this.flags = getUInt16(0x0C);
    }

    /**
     * @return Returns the attributeType.
     */
    public int getAttributeType() {
        return type;
    }

    /*
     * Flag |Description ------------------- 0x0001 |Compressed 0x4000
     * |Encrypted 0x8000 |Sparse
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Checks whether this attribute contains compressed data runs.
     *
     * @return {@code true} if the attribute contains compressed runs, {@code false} otherwise.
     */
    public boolean isCompressedAttribute() {
        return (getFlags() & 0x0001) != 0;
    }

    /**
     * @return Returns the nameLength.
     */
    public int getNameLength() {
        return getUInt8(0x09);
    }

    /**
     * @return Returns the nameOffset.
     */
    public int getNameOffset() {
        return getUInt16(0x0A);
    }

    /**
     * @return Returns the attributeID.
     */
    public int getAttributeID() {
        return getUInt16(0x0E);
    }

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        // if it is named fill the attribute name
        final int nameLength = getNameLength();
        if (nameLength > 0) {
            final char[] namebuf = new char[nameLength];
            final int nameOffset = getNameOffset();
            for (int i = 0; i < nameLength; i++) {
                namebuf[i] = getChar16(nameOffset + (i * 2));
            }
            return new String(namebuf);
        }
        return null;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() {
        return this.fileRecord;
    }

    /**
     * @return Returns the resident.
     */
    public boolean isResident() {
        return (getUInt8(0x08) == 0);
    }

    /**
     * Gets the length of this attribute in bytes.
     * 
     * @return the length
     */
    public int getSize() {
        return getUInt32AsInt(4);
    }

    /**
     * Generates a hex dump of the attribute's data.
     *
     * @return the hex dump.
     */
    public String hexDump() {
        int length = getBuffer().length - getOffset();
        byte[] data = new byte[length];
        getData(0, data, 0, data.length);
        return FSUtils.toString(data);
    }

    /**
     * Generates a debug string for the attribute.
     *
     * @return the debug string.
     */
    public abstract String toDebugString();

    /**
     * Create an NTFSAttribute instance suitable for the given attribute data.
     *
     * @param fileRecord the containing file record.
     * @param offset the offset to read from.
     * @return the attribute
     */
    public static NTFSAttribute getAttribute(FileRecord fileRecord, int offset) {
        final boolean resident = (fileRecord.getUInt8(offset + 0x08) == 0);
        final int type = fileRecord.getUInt32AsInt(offset + 0x00);

        switch (type) {
            case Types.STANDARD_INFORMATION:
                return new StandardInformationAttribute(fileRecord, offset);
            case Types.ATTRIBUTE_LIST:
                if (resident) {
                    return new AttributeListAttributeRes(fileRecord, offset);
                } else {
                    return new AttributeListAttributeNonRes(fileRecord, offset);
                }
            case Types.FILE_NAME:
                return new FileNameAttribute(fileRecord, offset);
            case Types.INDEX_ROOT:
                return new IndexRootAttribute(fileRecord, offset);
            case Types.INDEX_ALLOCATION:
                return new IndexAllocationAttribute(fileRecord, offset);
            case Types.REPARSE_POINT:
                return new ReparsePointAttribute(fileRecord, offset);
        }

        // check the resident flag
        if (resident) {
            // resident
            return new NTFSResidentAttribute(fileRecord, offset);
        } else {
            // non resident
            return new NTFSNonResidentAttribute(fileRecord, offset);
        }
    }
}
