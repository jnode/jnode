/*
 * $Id$
 */
package org.jnode.fs.ntfs;

/**
 * An NTFS file attribute that has its data stored inside the attribute.
 * 
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSResidentAttribute extends NTFSAttribute {

    /**
     * @param fileRecord
     * @param offset
     */
    public NTFSResidentAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the offset to the actual attribute. 
     * @return Returns the attributeOffset.
     */
    public int getAttributeOffset() {
        return getUInt16(0x14);
    }

    /**
     * @return Returns the indexedFlag.
     */
    public int getIndexedFlag() {
        return getUInt8(0x16);
    }

    public int getAttributeLength() {
        return (int) getUInt32(0x10);
    }
}