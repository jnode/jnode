package org.jnode.fs.ntfs.logfile;

import java.io.UnsupportedEncodingException;
import org.jnode.fs.ntfs.NTFSStructure;

/**
 * $LogFile client record.
 *
 * @author Luke Quinane
 */
public class LogClientRecord extends NTFSStructure {

    /**
     * Creates a new instance.
     *
     * @param buffer the buffer.
     * @param offset the offset.
     */
    public LogClientRecord(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    /**
     * Gets the oldest log file sequence number.
     *
     * @return the log file sequence number.
     */
    public long getOldestLsn() {
        return getInt64(0x00);
    }

    /**
     * Gets the oldest log file sequence number.
     *
     * @return the log file sequence number.
     */
    public long getRestartLsn() {
        return getInt64(0x08);
    }

    /**
     * Gets the offset to the previous client record, or {@code 0xffff} for none.
     *
     * @return the offset.
     */
    public int getPreviousClientOffset() {
        return getUInt16(0x10);
    }

    /**
     * Gets the offset to the next client record, or {@code 0xffff} for none.
     *
     * @return the offset.
     */
    public int getNextClientOffset() {
        return getUInt16(0x12);
    }

    /**
     * Gets the sequence number. Only set pre-Windows XP.
     *
     * @return the number.
     */
    public int getSequenceNumber() {
        return getUInt16(0x14);
    }

    /**
     * Gets the client name length, should always be {@code 8}.
     *
     * @return the length.
     */
    public int getClientNameLength() {
        return getUInt16(0x1c);
    }

    /**
     * Gets the client name, should always be {@code "NTFS"}.
     *
     * @return the name.
     */
    public String getClientName() {
        int length = getClientNameLength();
        byte[] buffer = new byte[length];
        getData(0x20, buffer, 0, length);
        try {
            return new String(buffer, "UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-16LE charset missing from JRE", e);
        }
    }

    /**
     * Gets a debug string for this instance.
     *
     * @return the debug string.
     */
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Log Client Record:[\n");
        builder.append("oldest-lsn: " + getOldestLsn() + "\n");
        builder.append("restart-lsn: " + getRestartLsn() + "\n");
        builder.append("previous-client: " + getPreviousClientOffset() + "\n");
        builder.append("next-client: " + getNextClientOffset() + "\n");
        builder.append("sequence-number: " + getSequenceNumber() + "\n");
        builder.append("name-length: " + getClientNameLength() + "\n");
        builder.append("name: " + getClientName() + "]");
        return builder.toString();
    }
}
