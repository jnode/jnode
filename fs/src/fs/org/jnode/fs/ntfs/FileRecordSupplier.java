package org.jnode.fs.ntfs;

import java.io.IOException;

/**
 * A FILE record supplier.
 */
public interface FileRecordSupplier {
    /**
     * Gets a record.
     *
     * @param referenceNumber the reference number.
     * @return the record, or {@code null} if the record cannot be looked up.
     * @throws IOException if an error occurs.
     */
    FileRecord getRecord(long referenceNumber) throws IOException;
}
