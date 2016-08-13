package org.jnode.fs.ntfs.attribute;

import org.jnode.fs.ntfs.FileRecord;

/**
 * A NTFS reparse point (symbolic link).
 *
 * @author Luke Quinane
 */
public class ReparsePointAttribute extends NTFSResidentAttribute {

    /**
     * Constructs the attribute.
     *
     * @param fileRecord the containing file record.
     * @param offset     offset of the attribute within the file record.
     */
    public ReparsePointAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the offset to the target name.
     *
     * @return the offset.
     */
    public int getTargetNameOffset() {
        return getUInt16(getAttributeOffset() + 0x8);
    }

    /**
     * Gets the length of the target name.
     *
     * @return the length.
     */
    public int getTargetNameLength() {
        return getUInt16(getAttributeOffset() + 0xa);
    }

    /**
     * Gets the offset to the print name.
     *
     * @return the offset.
     */
    public int getPrintNameOffset() {
        return getUInt16(getAttributeOffset() + 0xc);
    }

    /**
     * Gets the length of the print name.
     *
     * @return the length.
     */
    public int getPrintNameLength() {
        return getUInt16(getAttributeOffset() + 0xe);
    }

    /**
     * Gets the target name.
     *
     * @return the name.
     */
    public String getTargetName() {
        return getUtf16LEString(getAttributeOffset() + 0x10 + getTargetNameOffset(), getTargetNameLength() / 2);
    }

    /**
     * Gets the print name.
     *
     * @return the name.
     */
    public String getPrintName() {
        return getUtf16LEString(getAttributeOffset() + 0x10 + getPrintNameOffset(), getPrintNameLength() / 2);
    }
}
