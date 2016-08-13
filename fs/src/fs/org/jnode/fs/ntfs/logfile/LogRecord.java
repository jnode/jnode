package org.jnode.fs.ntfs.logfile;

import org.jnode.fs.ntfs.NTFSStructure;

/**
 * $LogFile log record.
 *
 * @author Luke Quinane
 */
public class LogRecord extends NTFSStructure {

    /**
     * The length of the fixed part of the record.
     */
    public static int HEADER_SIZE = 0x58;

    /**
     * The position inside the structure from which {@link #getClientDataLength()} is calculated.
     */
    public static int LENGTH_CALCULATION_OFFSET = 0x30;

    /**
     * The record type value for a check point record.
     */
    public static int RECORD_TYPE_CHECKPOINT = 0x2;

    /**
     * The flag that indicates the record crosses a page boundary.
     */
    public static int FLAG_CROSSES_PAGE = 0x1;

    /**
     * The 'LCNs to follow' value that indicates that there is a subsequent record.
     */
    public static int LCN_FOLLOWING_RECORD = 0x1;

    /**
     * The page size for log pages.
     */
    private final int pageSize;

    /**
     * The offset in the page to the log record data.
     */
    private final int logPageDataOffset;

    /**
     * Creates a new log file record.
     *  @param buffer the buffer.
     * @param offset the offset in the buffer to create the record at.
     * @param pageSize the page size.
     * @param logPageDataOffset the offset in the page to the log record data.
     */
    public LogRecord(byte[] buffer, int offset, int pageSize, int logPageDataOffset) {
        super(buffer, offset);

        this.pageSize = pageSize;
        this.logPageDataOffset = logPageDataOffset;
    }

    /**
     * Checks if the record appears to be valid.
     *
     * @return {@code true} if valid.
     */
    public boolean isValid() {
        return getLsn() != 0;
    }

    /**
     * Gets the log file sequence number for this record.
     *
     * @return the LSN.
     */
    public long getLsn() {
        return getInt64(0x00);
    }

    /**
     * Gets the client previous log file sequence number.
     *
     * @return the LSN.
     */
    public long getClientPreviousLsn() {
        return getInt64(0x08);
    }

    /**
     * Gets the client undo next log file sequence number.
     *
     * @return the LSN.
     */
    public long getClientUndoNextLsn() {
        return getInt64(0x10);
    }

    /**
     * Gets the client data length.
     *
     * @return the length.
     */
    public long getClientDataLength() {
        return getUInt32(0x18);
    }

    /**
     * Gets the sequence number or client index.
     *
     * @return the value.
     */
    public int getClientId() {
        return getUInt16(0x1c);
    }

    /**
     * Gets the record type.
     *
     * @return the value.
     */
    public long getRecordType() {
        return getUInt32(0x20);
    }

    /**
     * Gets the transaction ID.
     *
     * @return the transaction ID.
     */
    public long getTransactionId() {
        return getUInt32(0x24);
    }

    /**
     * Gets the log record flags.
     *
     * @return the log record flags.
     */
    public int getFlags() {
        return getUInt16(0x28);
    }

    /**
     * Gets an unsigned 16-bit integer which may or may not cross the log file page boundary.
     *
     * @param offset the offset to the field in this structure.
     * @return the value.
     */
    protected int getUInt16AcrossPages(int offset) {
        if (getCrossesPage()) {
            int offsetWithinPage = getOffset() % pageSize + offset;
            if (offsetWithinPage + 2 > pageSize) {
                return getUInt16(offsetWithinPage + logPageDataOffset);
            }
        }

        return getUInt16(offset);
    }

    /**
     * Gets an unsigned 32-bit integer which may or may not cross the log file page boundary.
     *
     * @param offset the offset to the field in this structure.
     * @return the value.
     */
    protected long getUInt32AcrossPages(int offset) {
        if (getCrossesPage()) {
            int offsetWithinPage = getOffset() % pageSize + offset;
            if (offsetWithinPage + 4 > pageSize) {
                return getUInt32(offsetWithinPage + logPageDataOffset);
            }
        }

        return getUInt32(offset);
    }

    /**
     * Copy (byte-array) data from a given offset which may or may not cross the log file page boundary.
     *
     * @param offset the offset to read from in this structure.
     * @param dst the destination to write to.
     * @param dstOffset the offset to write from.
     * @param length the length.
     */
    public final void getDataAcrossPages(int offset, byte[] dst, int dstOffset, int length) {
        if (getCrossesPage()) {
            int baseOffset = getOffset() + offset;
            int offsetWithinPage = baseOffset % pageSize;
            int pageOffset = baseOffset / pageSize;

            while (length > 0) {
                if (pageOffset > getBuffer().length) {
                    // Wrap back around to the start of the 'normal' area
                    pageOffset = LogFile.NORMAL_AREA_START * pageSize;
                }

                int endOffset = Math.min(offsetWithinPage + length, pageSize);
                int readLength = endOffset - offsetWithinPage;
                getData(pageOffset + offsetWithinPage, dst, dstOffset, readLength);

                length -= readLength;
                offsetWithinPage += readLength;
                dstOffset += readLength;

                if (offsetWithinPage >= pageSize || readLength == 0) {
                    offsetWithinPage = logPageDataOffset;
                    pageOffset += pageSize;
                }
            }
        }

        getData(offset, dst, dstOffset, length);
    }

    /**
     * Indicates whether this log record crosses a page boundary.
     *
     * @return {@code true} if it crosses a page boundary.
     */
    public boolean getCrossesPage() {
        return (getFlags() & FLAG_CROSSES_PAGE) == FLAG_CROSSES_PAGE;
    }

    /**
     * Gets the redo operation.
     *
     * @return the redo operation.
     */
    public int getRedoOperation() {
        return getUInt16AcrossPages(0x30);
    }

    /**
     * Gets the undo operation.
     *
     * @return the undo operation.
     */
    public int getUndoOperation() {
        return getUInt16AcrossPages(0x32);
    }

    /**
     * Gets the redo offset.
     *
     * @return the redo offset.
     */
    public int getRedoOffset() {
        return getUInt16AcrossPages(0x34);
    }

    /**
     * Gets the redo length.
     *
     * @return the redo length.
     */
    public int getRedoLength() {
        return getUInt16AcrossPages(0x36);
    }

    /**
     * Gets the undo offset.
     *
     * @return the undo offset.
     */
    public int getUndoOffset() {
        return getUInt16AcrossPages(0x38);
    }

    /**
     * Gets the undo length.
     *
     * @return the undo length.
     */
    public int getUndoLength() {
        return getUInt16AcrossPages(0x3a);
    }

    /**
     * Gets the target attribute.
     *
     * @return the attribute.
     */
    public int getTargetAttribute() {
        return getUInt16AcrossPages(0x3c);
    }

    /**
     * Gets the number of LCN list entries to follow.
     *
     * @return the number.
     */
    public int getLcnsToFollow() {
        return getUInt16AcrossPages(0x3e);
    }

    /**
     * Gets the record offset.
     *
     * @return the offset.
     */
    public int getRecordOffset() {
        return getUInt16AcrossPages(0x40);
    }

    /**
     * Gets the attribute offset.
     *
     * @return the offset.
     */
    public int getAttributeOffset() {
        return getUInt16AcrossPages(0x42);
    }

    /**
     * Gets the MFT cluster index.
     *
     * @return the value.
     */
    public int getMftClusterIndex() {
        return getUInt16AcrossPages(0x44);
    }

    /**
     * Gets the target VCN.
     *
     * @return the target VCN.
     */
    public long getTargetVcn() {
        return getUInt32AcrossPages(0x48);
    }

    /**
     * Gets the target LCN.
     *
     * @return the target LCN.
     */
    public long getTargetLcn() {
        return getUInt32AcrossPages(0x50);
    }

    /**
     * Gets the redo data for the record.
     *
     * @param buffer the buffer to write into.
     */
    public void getRedoData(byte[] buffer) {
        getDataAcrossPages(0x30 + getRedoOffset(), buffer, 0, getRedoLength());
    }

    /**
     * Gets the undo data for the record.
     *
     * @param buffer the buffer to write into.
     */
    public void getUndoData(byte[] buffer) {
        getDataAcrossPages(0x30 + getUndoOffset(), buffer, 0, getUndoLength());
    }

    @Override
    public String toString() {
        String type = "";
        if (getRecordType() == RECORD_TYPE_CHECKPOINT) {
            type = "checkpoint";
        } else {
            OperationCode undoCode = OperationCode.fromCode(getUndoOperation());
            if (undoCode != null) {
                type += undoCode.name();
            } else {
                type += "unknown: " + getUndoOperation();
            }

            type += " --- ";

            OperationCode redoCode = OperationCode.fromCode(getRedoOperation());
            if (redoCode != null) {
                type += redoCode.name();
            } else {
                type += "unknown: " + getRedoOperation();
            }
        }
        return String.format("log-record:[%d - %d %s]", getLsn(), getTransactionId(), type);
    }

    /**
     * Gets a debug string for this instance.
     *
     * @return the debug string.
     */
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Log Record:[\n");
        builder.append("lsn: " + getLsn() + "\n");
        builder.append("prev-lsn: " + getClientPreviousLsn() + "\n");
        builder.append("undo-lsn: " + getClientUndoNextLsn() + "\n");
        builder.append("data-length: " + getClientDataLength() + "\n");
        builder.append("client-id: " + getClientId() + "\n");
        builder.append("record-type: " + getRecordType() + "\n");
        builder.append("transaction-id: " + getTransactionId() + "\n");
        builder.append("flags: " + getFlags() + "\n");
        builder.append("redo: " + getRedoOperation() + "\n");
        builder.append("undo: " + getUndoOperation() + "\n");
        builder.append("redo-offset: " + getRedoOffset() + "\n");
        builder.append("redo-length: " + getRedoLength() + "\n");
        builder.append("undo-offset: " + getUndoOffset() + "\n");
        builder.append("undo-length: " + getUndoLength() + "\n");
        builder.append("target-attribute: " + getTargetAttribute() + "\n");
        builder.append("lcns-to-follow: " + getLcnsToFollow() + "\n");
        builder.append("record-offset: " + getRecordOffset() + "\n");
        builder.append("attribute-offset: " + getAttributeOffset() + "\n");
        builder.append("MFT-cluster-index: " + getMftClusterIndex() + "\n");
        builder.append("target-vcn: " + getTargetVcn() + "\n");
        builder.append("target-lcn: " + getTargetLcn() + "]");
        return builder.toString();
    }
}
