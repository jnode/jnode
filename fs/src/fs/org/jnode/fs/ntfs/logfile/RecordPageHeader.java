package org.jnode.fs.ntfs.logfile;

import java.io.IOException;
import org.jnode.fs.ntfs.NTFSRecord;

/**
 * $LogFile record page header.
 *
 * @author Luke Quinane
 */
public class RecordPageHeader extends NTFSRecord {

    /**
     * The size of this structure.
     */
    public static final int HEADER_SIZE = 0x28;

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
     * @throws IOException if an error occurs during fixup.
     */
    public RecordPageHeader(byte[] buffer, int offset) throws IOException {
        super(true, buffer, offset);
    }

    /**
     * Checks if this header seems to be valid.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return getMagic() == Magic.RCRD;
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
    public int getNextRecordOffset() {
        return getInt16(0x18);
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
