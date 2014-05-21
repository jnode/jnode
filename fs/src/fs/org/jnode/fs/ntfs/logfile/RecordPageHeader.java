package org.jnode.fs.ntfs.logfile;

import org.jnode.fs.ntfs.NTFSStructure;

/**
 * $LogFile record page header.
 *
 * @author Luke Quinane
 */
public class RecordPageHeader extends NTFSStructure {

    /**
     * Magic constants
     */
    public static class Magic {
        /**
         * Corrupt record
         */
        public static final int RCRD = 0x44524352;
    }

    /**
     * Creates a new log file record page header.
     *
     * @param buffer the buffer.
     * @param offset the offset.
     */
    public RecordPageHeader(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    /**
     * Gets the magic value of this record.
     *
     * @return the magic
     */
    public int getMagic() {
        return getUInt32AsInt(0x00);
    }

    /**
     * Gets the update sequence offset.
     *
     * @return the offset.
     */
    public int getUpdateSequenceOffset() {
        return getUInt16(0x04);
    }

    /**
     * Gets the update sequence count.
     *
     * @return the count.
     */
    public int getUpdateSequenceCount() {
        return getUInt16(0x06);
    }

    /**
     * Gets the last log file sequence number or file offset.
     *
     * @return the value.
     */
    public long getLastLsnOrFileOffset() {
        return getInt64(0x08);
    }

    /**
     * Gets the flags.
     *
     * @return the flags.
     */
    public long getFlags() {
        return getUInt32(0x10);
    }

    /**
     * Gets the page count.
     *
     * @return the page count.
     */
    public int getPageCount() {
        return getUInt16(0x14);
    }

    /**
     * Gets the page position.
     *
     * @return the page position.
     */
    public int getPagePosition() {
        return getUInt16(0x16);
    }

    /**
     * Gets the next record offset.
     *
     * @return the value.
     */
    public long getNextRecordOffset() {
        return getInt64(0x18);
    }

    /**
     * Gets the last end LSN.
     *
     * @return the value.
     */
    public long getLastEndLsn() {
        return getInt64(0x20);
    }

    /**
     * Gets a debug string for this instance.
     *
     * @return the debug string.
     */
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Record Page Header:[\n");
        builder.append("update-sequence-offset: " + getUpdateSequenceOffset() + "\n");
        builder.append("update-sequence-count: " + getUpdateSequenceCount() + "\n");
        builder.append("lsn-or-file-offset: " + getLastLsnOrFileOffset() + "\n");
        builder.append("flags: " + getFlags() + "\n");
        builder.append("page-count: " + getPageCount() + "\n");
        builder.append("page-position: " + getPagePosition() + "\n");
        builder.append("next-record: " + getNextRecordOffset() + "\n");
        builder.append("last-end-lsn: " + getLastEndLsn() + "]");
        return builder.toString();
    }
}
