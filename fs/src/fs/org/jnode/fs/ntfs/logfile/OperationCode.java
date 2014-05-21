package org.jnode.fs.ntfs.logfile;

/**
 * The $LogFile redo/undo codes.
 *
 * {@see http://forensicinsight.org/wp-content/uploads/2013/06/F-INSIGHT-NTFS-Log-TrackerEnglish.pdf}
 */
public enum OperationCode {
    NOOP(0x00),
    COMPENSATION_LOG_RECOR(0x01),
    INITIALIZE_FILE_RECORD_SEGMENT(0x02),
    DEALLOCATE_FILE_RECORD_SEGMENT(0x03),
    WRITE_END_OF_FILE_RECORD_SEGMENT(0x04),
    CREATE_ATTRIBUTE(0x05),
    DELETE_ATTRIBUTE(0x06),
    UPDATE_RESIDENT_VALUE(0x07),
    UPDATE_NON_RESIDENT_VALUE(0x08),
    UPDATE_MAPPING_PAIRS(0x09),
    DELETE_DIRTY_CLUSTERS(0x0A),
    SET_NEW_ATTRIBUTE_SIZES(0x0B),
    ADD_INDEX_ENTRY_ROOT(0x0C),
    DELETE_INDEX_ENTRY_ROOT(0x0D),
    ADD_INDEX_ENTRY_ALLOCATION(0x0F),
    SET_INDEX_ENTRY_VEN_ALLOCATION(0x12),
    UPDATE_FILE_NAME_ROOT(0x13),
    UPDATE_FILE_NAME_ALLOCATION(0x14),
    SET_BITS_IN_NON_RESIDENT_BITMAP(0x15),
    CLEAR_BITS_IN_NON_RESIDENT_BITMAP(0x16),
    PREPARE_TRANSACTION(0x19),
    COMMIT_TRANSACTION(0x1A),
    FORGET_TRANSACTION(0x1B),
    OPEN_NON_RESIDENT_ATTRIBUTE(0x1C),
    DIRTY_PAGE_TABLE_DUMP(0x1F),
    TRANSACTION_TABLE_DUMP(0x20),
    UPDATE_RECORD_DATA_ROOT(0x21);

    private final int code;

    private OperationCode(int code) {
        this.code = code;
    }

    /**
     * Looks up the operation code from the given raw value.
     *
     * @param code the value to look up.
     * @return the operation code, or {@code null} if no matching code was found.
     */
    public static OperationCode fromCode(int code) {
        for (OperationCode operationCode : values()) {
            if (operationCode.code == code) {
                return operationCode;
            }
        }

        return null;
    }
}
