/*
 * $Id$
 */
package org.jnode.fs.ntfs;


/**
 * Structure accessor of an Index Entry.
 * 
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexEntry extends NTFSStructure {

    private final FileRecord parentFileRecord;

    /**
     * Initialize this instance.
     * @param parentFileRecord
     * @param buffer
     * @param offset
     */
    public IndexEntry(FileRecord parentFileRecord, byte[] buffer, int offset) {
        super(buffer, offset);
        this.parentFileRecord = parentFileRecord;
    }

    /**
     * Initialize this instance.
     * @param parentFileRecord
     * @param parent
     * @param offset
     */
    public IndexEntry(FileRecord parentFileRecord, NTFSStructure parent, int offset) {
        super(parent, offset);
        this.parentFileRecord = parentFileRecord;
    }

    public boolean hasSubNodes() {
        return (getFlags() & 0x01) != 0;
    }
    
    /**
     * Gets the length of this index entry in bytes.
     * @return
     */
    public int getSize() {
        return getUInt16(0x08);
    }

    /**
     * Gets the flags of this index entry.
     * @return
     */
    public int getFlags() {
        return getUInt8(0x0C);
    }

    public boolean isLastIndexEntryInSubnode() {
        return (getFlags() & 0x02) != 0;
    }

    /**
     * Gets the filename namespace.
     * @see FileNameAttribute.NameSpace
     * @return
     */
    public int getNameSpace() {
        return getUInt8(0x51);
    }

    public boolean isDirectory() {
        return (getUInt32(0x48) & 0x10000000L) != 0;
    }

    public String getFileName() {
        return new String(this.getFileNameAsCharArray());
    }

    public long getFileReferenceNumber() {
        return getUInt48(0x00);
    }

    public long getFileFlags() {
        return getInt64(0x48);
    }

    public long getRealFileSize() {
        return getInt64( 0x40);
    }

    /**
     * @return Returns the parentFileRecord.
     */
    public FileRecord getParentFileRecord() {
        return parentFileRecord;
    }

    public long getSubnodeVCN() {
        return getUInt32(getSize() - 8);
    }
    
    private char[] getFileNameAsCharArray() {
        final int len = getUInt8(0x50);
        final char[] name = new char[ len];
        for (int i = 0; i < len; i++) {
            name[ i] = getChar16(0x52 + (i * 2));
        }
        return name;
    }
}