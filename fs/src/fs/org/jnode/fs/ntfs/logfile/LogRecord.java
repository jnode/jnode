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
     * Creates a new log file record.
     *
     * @param buffer the buffer.
     */
    public LogRecord(byte[] buffer, int offset) {
        super(buffer, offset);
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
     * Gets the redo operation.
     *
     * @return the redo operation.
     */
    public int getRedoOperation() {
        return getUInt16(0x30);
    }

    /**
     * Gets the undo operation.
     *
     * @return the undo operation.
     */
    public int getUndoOperation() {
        return getUInt16(0x32);
    }

    /**
     * Gets the redo offset.
     *
     * @return the redo offset.
     */
    public int getRedoOffset() {
        return getUInt16(0x34);
    }

    /**
     * Gets the redo length.
     *
     * @return the redo length.
     */
    public int getRedoLength() {
        return getUInt16(0x36);
    }

    /**
     * Gets the undo offset.
     *
     * @return the undo offset.
     */
    public int getUndoOffset() {
        return getUInt16(0x38);
    }

    /**
     * Gets the undo length.
     *
     * @return the undo length.
     */
    public int getUndoLength() {
        return getUInt16(0x3a);
    }

    /**
     * Gets the target attribute.
     *
     * @return the attribute.
     */
    public int getTargetAttribute() {
        return getUInt16(0x3c);
    }

    /**
     * Gets the number of LCN list entries to follow.
     *
     * @return the number.
     */
    public int getLcnsToFollow() {
        return getUInt16(0x3e);
    }

    /**
     * Gets the record offset.
     *
     * @return the offset.
     */
    public int getRecordOffset() {
        return getUInt16(0x40);
    }

    /**
     * Gets the attribute offset.
     *
     * @return the offset.
     */
    public int getAttributeOffset() {
        return getUInt16(0x42);
    }

    /**
     * Gets the MFT cluster index.
     *
     * @return the value.
     */
    public int getMftClusterIndex() {
        return getUInt16(0x44);
    }

    /**
     * Gets the target VCN.
     *
     * @return the target VCN.
     */
    public long getTargetVcn() {
        return getUInt32(0x48);
    }

    /**
     * Gets the target LCN.
     *
     * @return the target LCN.
     */
    public long getTargetLcn() {
        return getUInt32(0x50);
    }

    @Override
    public String toString() {
        return String.format("log-record:[%d]", getLsn());
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
