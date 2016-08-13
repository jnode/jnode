package org.jnode.fs.ntfs.logfile;

/**
 * The $LogFile redo/undo codes.
 *
 * {@see http://forensicinsight.org/wp-content/uploads/2013/06/F-INSIGHT-NTFS-Log-TrackerEnglish.pdf}
 */
public enum OperationCode {
    NOOP(0x00, "No-op"),
    COMPENSATION_LOG_RECORD(0x01, "Compensation Log Record"),
    INITIALIZE_FILE_RECORD_SEGMENT(0x02, "Init File Record Segment"),
    DEALLOCATE_FILE_RECORD_SEGMENT(0x03, "Deallocate File Record Segment"),
    WRITE_END_OF_FILE_RECORD_SEGMENT(0x04, "Write End of File Record Segment"),
    CREATE_ATTRIBUTE(0x05, "Create Attribute"),
    DELETE_ATTRIBUTE(0x06, "Delete Attribute"),
    UPDATE_RESIDENT_VALUE(0x07, "Update Resident Value"),
    UPDATE_NON_RESIDENT_VALUE(0x08, "Update Non-Resident Value"),
    UPDATE_MAPPING_PAIRS(0x09, "Update Mapping Pairs"),
    DELETE_DIRTY_CLUSTERS(0x0A, "Delete Dirty Clusters"),
    SET_NEW_ATTRIBUTE_SIZES(0x0B, "Set New Attribute Sizes"),
    ADD_INDEX_ENTRY_ROOT(0x0C, "Add Index Entry Root"),
    DELETE_INDEX_ENTRY_ROOT(0x0D, "Delete Index Entry Root"),
    DELETE_INDEX_ENTRY_ALLOCATION(0x0E, "Delete Index Entry Allocation"),
    ADD_INDEX_ENTRY_ALLOCATION(0x0F, "Add Index Entry Allocation"),
    SET_INDEX_ENTRY_VEN_ALLOCATION(0x12, "Set Index Entry Ven Allocation"),
    UPDATE_FILE_NAME_ROOT(0x13, "Update File Name Root"),
    UPDATE_FILE_NAME_ALLOCATION(0x14, "Update File Name Allocation"),
    SET_BITS_IN_NON_RESIDENT_BITMAP(0x15, "Set Bits in Non-Resident Bitmap"),
    CLEAR_BITS_IN_NON_RESIDENT_BITMAP(0x16, "Clear Bits in Non-Resident Bitmap"),
    PREPARE_TRANSACTION(0x19, "Prepare Transaction"),
    COMMIT_TRANSACTION(0x1A, "Commit Transaction"),
    FORGET_TRANSACTION(0x1B, "Forget Transaction"),
    OPEN_NON_RESIDENT_ATTRIBUTE(0x1C, "Open Non-Resident Attribute"),
    DIRTY_PAGE_TABLE_DUMP(0x1F, "Dirty Page Table Dump"),
    TRANSACTION_TABLE_DUMP(0x20, "Transaction Table Dump"),
    UPDATE_RECORD_DATA_ROOT(0x21, "Update Record Data Root");

    private final int code;
    private final String description;

    private OperationCode(int code, String description) {
        this.code = code;
        this.description = description;
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

    /**
     * Looks up the description for the given operation code.
     *
     * @param code the value to look up.
     * @return the description.
     */
    public static String lookupDescription(int code) {
        for (OperationCode operationCode : values()) {
            if (operationCode.code == code) {
                return operationCode.description;
            }
        }

        return String.format("Unknown operation: 0x%x", code);
    }
}
