package org.jnode.fs.ntfs.logfile;

import java.io.IOException;
import org.jnode.fs.ntfs.NTFSRecord;

/**
 * $LogFile restart page header
 *
 * @author Luke Quinane
 */
public class RestartPageHeader extends NTFSRecord {

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
     * @param offset the offset to create the structure at.
     * @throws IOException if an error occurs during fixup.
     */
    public RestartPageHeader(byte[] buffer, int offset) throws IOException {
        super(true, buffer, offset);
    }

    /**
     * Checks if this header seems to be valid.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return getMagic() == Magic.RSTR || getMagic() == Magic.CHKD;
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
        builder.append("chkdsk-lsn: " + getCheckDiskLsn() + "\n");
        builder.append("system-page-size: " + getSystemPageSize() + "\n");
        builder.append("log-page-size: " + getLogPageSize() + "\n");
        builder.append("restart-offset: " + getRestartOffset() + "\n");
        builder.append("minor-version: " + getMinorVersion() + "\n");
        builder.append("major-version: " + getMajorVersion() + "]");
        return builder.toString();
    }
}