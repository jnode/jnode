package org.jnode.fs.ntfs.logfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSVolume;
import org.jnode.fs.ntfs.attribute.NTFSAttribute;
import org.jnode.fs.util.FSUtils;
import org.jnode.util.LittleEndian;

/**
 * $LogFile
 *
 * @author Luke Quinane
 */
public class LogFile {

    /**
     * My logger
     */
    protected static final Logger log = Logger.getLogger(LogFile.class);

    /**
     * The start of the normal area (in pages).
     */
    private static final int NORMAL_AREA_START = 4;

    /**
     * The list of open log clients.
     */
    private final List<LogClientRecord> logClients = new ArrayList<LogClientRecord>();

    /**
     * The map of offset to record page headers.
     */
    private Map<Integer, RecordPageHeader> offsetPageMap = new HashMap<Integer, RecordPageHeader>();

    /**
     * The list log records.
     */
    private final List<LogRecord> logRecords = new ArrayList<LogRecord>();

    /**
     * The restart page header.
     */
    private final RestartPageHeader restartPageHeader;

    /**
     * The restart area.
     */
    private final RestartArea restartArea;

    /**
     * The $LogFile page size.
     */
    private final int logPageSize;

    /**
     * The $LogFile size.
     */
    private final long logFileLength;

    /**
     * The buffer that holds the $LogFile data.
     */
    private final byte[] logFileBuffer;

    /**
     * Indicates whether the $LogFile contents need to be checked when reading MFT data.
     */
    private boolean cleanlyShutdown = true;

    /**
     * Creates a new instance.
     *
     * @param fileRecord the file record to read the $LogFile data from.
     * @throws IOException if an error occurs.
     */
    public LogFile(FileRecord fileRecord) throws IOException {
        // Read in the log file data
        logFileLength = fileRecord.getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
        logFileBuffer = new byte[(int) logFileLength];
        fileRecord.readData(0, logFileBuffer, 0, (int) logFileLength);

        // Read in the restart area info
        restartPageHeader = getNewestRestartPageHeader(logFileBuffer);
        int restartAreaOffset = restartPageHeader.getRestartOffset();
        logPageSize = restartPageHeader.getLogPageSize();
        restartArea = new RestartArea(logFileBuffer, restartAreaOffset);

        if ((restartArea.getFlags() & RestartArea.VOLUME_CLEANLY_UNMOUNTED) != RestartArea.VOLUME_CLEANLY_UNMOUNTED) {
            log.info("Volume not cleanly unmounted");
            cleanlyShutdown = false;
        }

        // Read in any open log client records
        int logClientCount = restartArea.getLogClients();
        if (logClientCount != RestartArea.LOGFILE_NO_CLIENT) {
            log.info(String.format("Found %d open log clients", logClientCount));
            cleanlyShutdown = false;

            int logClientOffset = restartAreaOffset + restartArea.getClientArrayOffset();
            LogClientRecord logClientRecord = new LogClientRecord(logFileBuffer, logClientOffset);
            logClients.add(logClientRecord);

            for (int i = 1; i < logClientCount; i++) {
                logClientOffset = restartAreaOffset + logClientRecord.getNextClientOffset();
                logClientRecord = new LogClientRecord(logFileBuffer, logClientOffset);
                logClients.add(logClientRecord);
            }
        }

        int offset = findOldestPageOffset(fileRecord.getVolume());
        int recordOffset = 0;
        int logPageCount = (int) ((logFileLength - NORMAL_AREA_START * logPageSize) / logPageSize);

        // Read in each log page
        for (int pageNumber = 0; pageNumber < logPageCount; pageNumber++) {
            RecordPageHeader pageHeader = offsetPageMap.get(offset);

            if (pageHeader != null && pageHeader.isValid()) {
                // Ensure that the record offset is within the page and beyond the page header
                recordOffset = recordOffset % logPageSize;
                if (recordOffset != restartArea.getLogPageDataOffset()) {
                    recordOffset += restartArea.getLogPageDataOffset();
                }

                // Read in the page's log records
                while (recordOffset <= pageHeader.getNextRecordOffset()) {
                    if (recordOffset + LogRecord.LENGTH_CALCULATION_OFFSET > logPageSize) {
                        // No more room for records in this page, call again to search the next page
                        recordOffset = restartArea.getLogPageDataOffset();
                        break;
                    }

                    // Get the offset to the next record in the buffer rounded up to an 8-byte boundary
                    recordOffset = FSUtils.roundUpToBoundary(8, recordOffset);
                    LogRecord logRecord = new LogRecord(logFileBuffer, offset + recordOffset, logPageSize,
                        restartArea.getLogPageDataOffset());

                    if (logRecord.isValid()) {
                        logRecords.add(logRecord);
                        recordOffset += LogRecord.LENGTH_CALCULATION_OFFSET + (int) logRecord.getClientDataLength();
                    } else {
                        // Seems to be the end of valid records for this page
                        recordOffset = restartArea.getLogPageDataOffset();
                        break;
                    }
                }

            } else {
                log.warn("Found an invalid LogFile page: " + pageHeader + " at offset: " + offset);
            }

            offset += logPageSize;
            if (offset >= logFileLength) {
                // Wrap around to the start of the 'normal' area
                offset = NORMAL_AREA_START * logPageSize;
            }
        }
    }

    /**
     * Finds the offset to the oldest page, i.e. the one with the lowest LSN.
     *
     * @param volume the volume that holds the log file.
     * @return the offset to the oldest page.
     * @throws IOException if an error occurs.
     */
    private int findOldestPageOffset(NTFSVolume volume) throws IOException {
        TreeMap<Long, RecordPageHeader> lsnPageMap = new TreeMap<Long, RecordPageHeader>();
        Map<RecordPageHeader, Integer> pageOffsetMap = new HashMap<RecordPageHeader, Integer>();

        // Read in all the page header records. The first two pages are the restart area, and the next two pages are the
        // buffer page area, so start reading in page headers from the fifth page. This is the start of the 'normal
        // area'.

        for (int offset = 4 * logPageSize; offset < logFileLength; offset += logPageSize) {
            int magic = LittleEndian.getInt32(logFileBuffer, offset);

            if (magic != RecordPageHeader.Magic.RCRD) {
                // Bad page magic, possibly an uninitialised page
                continue;
            }

            RecordPageHeader pageHeader = new RecordPageHeader(volume, logFileBuffer, offset);
            offsetPageMap.put(offset, pageHeader);

            // If the last-end-LSN is zero then the page only contains data from the log record on the last page. I.e.
            // it has no new entries, so skip it
            if (pageHeader.isValid() && pageHeader.getLastEndLsn() != 0) {
                lsnPageMap.put(pageHeader.getLastEndLsn(), pageHeader);
                pageOffsetMap.put(pageHeader, offset);
            }
        }

        RecordPageHeader oldestPage = lsnPageMap.firstEntry().getValue();
        return pageOffsetMap.get(oldestPage);
    }

    /**
     * Gets the restart page header that corresponds to the restart page with the highest current LSN.
     *
     * @param buffer the buffer to read from.
     * @return the header.
     */
    private RestartPageHeader getNewestRestartPageHeader(byte[] buffer) {
        RestartPageHeader restartPageHeader1 = new RestartPageHeader(buffer, 0);
        if (!restartPageHeader1.isValid()) {
            throw new IllegalStateException("Restart header has invalid magic: " + restartPageHeader1.getMagic());
        } else if (restartPageHeader1.getMagic() == RestartPageHeader.Magic.CHKD) {
            log.warn("First $LogFile restart header has check disk magic");
        }

        RestartPageHeader restartPageHeader2 = new RestartPageHeader(buffer, restartPageHeader1.getLogPageSize());
        if (!restartPageHeader2.isValid()) {
            throw new IllegalStateException("Second restart header has invalid magic: " + restartPageHeader2.getMagic());
        }  else if (restartPageHeader2.getMagic() == RestartPageHeader.Magic.CHKD) {
            log.warn("Second $LogFile restart header has check disk magic");
        }

        int restartAreaOffset1 = restartPageHeader1.getRestartOffset();
        int restartAreaOffset2 = restartPageHeader2.getRestartOffset();
        RestartArea restartArea1 = new RestartArea(buffer, restartAreaOffset1);
        RestartArea restartArea2 = new RestartArea(buffer, restartPageHeader1.getLogPageSize() + restartAreaOffset2);

        // Pick the restart page with the highest current LSN
        if (restartArea1.getCurrentLsn() >= restartArea2.getCurrentLsn()) {
            return restartPageHeader1;
        } else {
            return restartPageHeader2;
        }
    }

    /**
     * Checks whether the log file seems to be cleanly shutdown.
     *
     * @return {@code true} if cleanly shutdown.
     */
    public boolean isCleanlyShutdown() {
        return cleanlyShutdown;
    }

    /**
     * Gets the log file records for this log file.
     *
     * @return the records.
     */
    public List<LogRecord> getLogRecords() {
        return logRecords;
    }
}
