/*
 * $Id$
 */
package org.jnode.fs.ntfs;

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
            final char[] namebuf = new char[ nameLength];
            final int nameOffset = getNameOffset();
            for (int i = 0; i < nameLength; i++) {
                namebuf[ i] = getChar16(nameOffset + (i * 2));
            }
            return new String(namebuf);
        }
        return null;
    }

    /**
     * @return Returns the volume.
     */

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
     * @return
     */
    public int getSize() {
        return getUInt32AsInt(4);
    }

    /**
     * Create an NTFSAttribute instance suitable for the given attribute data.
     * 
     * @param fileRecord
     * @param offset
     * @return
     */
    public static NTFSAttribute getAttribute(FileRecord fileRecord,
            int offset) {
        final int type = fileRecord.getUInt32AsInt(offset + 0x00);

        switch (type) {
        case Types.FILE_NAME:
            return new FileNameAttribute(fileRecord, offset);
        case Types.INDEX_ROOT:
            return new IndexRootAttribute(fileRecord, offset);
        case Types.INDEX_ALLOCATION:
            return new IndexAllocationAttribute(fileRecord, offset);
        }

        // check the resident flag
        if (fileRecord.getUInt8(offset + 0x08) == 0) {
            // resident
            return new NTFSResidentAttribute(fileRecord, offset);
        } else {
            // non resident
            return new NTFSNonResidentAttribute(fileRecord, offset);
        }
    }
}