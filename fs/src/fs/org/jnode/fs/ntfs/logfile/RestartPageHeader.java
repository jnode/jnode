package org.jnode.fs.ntfs.logfile;

import org.jnode.fs.ntfs.NTFSStructure;

/**
 * $LogFile restart page header
 *
 * @author Luke Quinane
 */
public class RestartPageHeader extends NTFSStructure {

    /**
     * Magic constants
     */
    public static class Magic {
        /**
         * Restart page header.
         */
        public static final int RSTR = 0x52545352;

        /**
         * Check disk.
         */
        public static final int CHKD = 0x444B4843;
    }

    /**
     * Creates a new log file restart page header.
     *
     * @param buffer the buffer.
     */
    public RestartPageHeader(byte[] buffer) {
        super(buffer, 0);
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
     * Gets the check disk log file sequence number. Only used when the magic number is {@link Magic#CHKD}.
     *
     * @return the log file sequence number.
     */
    public long getCheckDiskLsn() {
        return getInt64(0x08);
    }

    /**
     * Gets the system page size.
     *
     * @return the system page size.
     */
    public int getSystemPageSize() {
        return getUInt32AsInt(0x10);
    }

    /**
     * Gets the log page size.
     *
     * @return the log page size.
     */
    public int getLogPageSize() {
        return getUInt32AsInt(0x14);
    }

    /**
     * Gets the offset to the restart record.
     *
     * @return the offset.
     */
    public int getRestartOffset() {
        return getUInt16(0x18);
    }

    /**
     * Gets the minor version.
     *
     * @return the version.
     */
    public int getMinorVersion() {
        return getInt16(0x1a);
    }

    /**
     * Gets the major version.
     *
     * @return the version.
     */
    public int getMajorVersion() {
        return getUInt16(0x1c);
    }

    /**
     * Gets a debug string for this instance.
     *
     * @return the debug string.
     */
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Restart Page Header:[\n");
        builder.append("update-sequence-offset: " + getUpdateSequenceOffset() + "\n");
        builder.append("update-sequence-count: " + getUpdateSequenceCount() + "\n");
        builder.append("chkdsk-lsn: " + getCheckDiskLsn() + "\n");
        builder.append("system-page-size: " + getSystemPageSize() + "\n");
        builder.append("log-page-size: " + getLogPageSize() + "\n");
        builder.append("restart-offset: " + getRestartOffset() + "\n");
        builder.append("minor-version: " + getMinorVersion() + "\n");
        builder.append("major-version: " + getMajorVersion() + "]");
        return builder.toString();
    }
}