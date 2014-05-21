package org.jnode.fs.ntfs.logfile;

import org.jnode.fs.ntfs.NTFSStructure;

/**
 * $LogFile restart area record.
 *
 * @author Luke Quinane
 */
public class RestartArea extends NTFSStructure {

    /**
     * Creates a new instance.
     *
     * @param buffer the buffer.
     * @param offset the offset.
     */
    public RestartArea(byte[] buffer, int offset) {
        super(buffer, offset);
    }

    /**
     * Gets the current log file sequence number.
     *
     * @return the log file sequence number.
     */
    public long getCurrentLsn() {
        return getInt64(0x00);
    }

    /**
     * Gets the number of log client records.
     *
     * @return the number of log clients.
     */
    public int getLogClients() {
        return getUInt16(0x08);
    }

    /**
     * Gets the number of free clients.
     *
     * @return the number of free clients.
     */
    public int getClientFreeList() {
        return getUInt16(0x0a);
    }

    /**
     * Gets the flags.
     *
     * @return the flags.
     */
    public long getFlags() {
        return getUInt32(0x0c);
    }

    /**
     * Gets the sequence number bits.
     *
     * @return the sequence number bits.
     */
    public long getSequenceNumberBits() {
        return getUInt32(0x10);
    }

    /**
     * Gets the length of the restart area.
     *
     * @return the length.
     */
    public int getRestartAreaLength() {
        return getUInt16(0x14);
    }

    /**
     * Gets the offset to the first client record.
     *
     * @return the length.
     */
    public int getClientArrayOffset() {
        return getUInt16(0x16);
    }

    /**
     * Gets the size of the log file.
     *
     * @return the size.
     */
    public long getFileSize() {
        return getInt64(0x18);
    }

    /**
     * Gets the last LSN data length.
     *
     * @return the length.
     */
    public long getLastLsnDataLength() {
        return getUInt32(0x20);
    }

    /**
     * Gets the record length.
     *
     * @return the length.
     */
    public int getRecordLength() {
        return getUInt16(0x24);
    }

    /**
     * Gets the log page data offset.
     *
     * @return the offset.
     */
    public int getLogPageDataOffset() {
        return getUInt16(0x26);
    }
    /**
     * Gets a debug string for this instance.
     *
     * @return the debug string.
     */
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Restart Area:[\n");
        builder.append("current-lsn: " + getCurrentLsn() + "\n");
        builder.append("log-clients: " + getLogClients() + "\n");
        builder.append("client-free-list: " + getClientFreeList() + "\n");
        builder.append("flags: " + getFlags() + "\n");
        builder.append("seq-number-bits: " + getSequenceNumberBits() + "\n");
        builder.append("restart-area-length: " + getRestartAreaLength() + "\n");
        builder.append("client-array-offset: " + getClientArrayOffset() + "\n");
        builder.append("file-size: " + getFileSize() + "\n");
        builder.append("last-lsn-data-length: " + getLastLsnDataLength() + "\n");
        builder.append("record-length: " + getRecordLength() + "\n");
        builder.append("log-page-data-offset: " + getLogPageDataOffset() + "]");
        return builder.toString();
    }
}