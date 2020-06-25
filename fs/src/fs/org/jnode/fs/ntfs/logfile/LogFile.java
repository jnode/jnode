package org.jnode.fs.ntfs.logfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.jnode.fs.ntfs.FileRecord;
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
    public static final int NORMAL_AREA_START = 4;

    /**
     * The list of open log clients.
     */
    private final List<LogClientRecord> logClients = new ArrayList<LogClientRecord>();

    /**
     * The offset to the oldest page.
     */
    private final int oldestPageOffset;

    /**
     * The map of offset to record page headers.
     */
    private Map<Integer, RecordPageHeader> offsetPageMap = new LinkedHashMap<Integer, RecordPageHeader>();

    /**
     * The map of LSN to log record.
     */
    private Map<Long, LogRecord> lsnLogRecordMap;

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
        int restartAreaOffset = restartPageHeader.getOffset() + restartPageHeader.getRestartOffset();
        logPageSize = restartPageHeader.getLogPageSize();
        restartArea = new RestartArea(logFileBuffer, restartAreaOffset);

        if ((restartArea.getFlags() & RestartArea.VOLUME_CLEANLY_UNMOUNTED) != RestartArea.VOLUME_CLEANLY_UNMOUNTED) {
            log.info("Volume not cleanly unmounted");
            cleanlyShutdown = false;

        } else {
            log.info("Volume marked as cleanly unmounted");
        }

        // Read in any open log client records
        int logClientCount = restartArea.getLogClients();
        if (logClientCount != RestartArea.LOGFILE_NO_CLIENT) {
            log.info(String.format("Found %d open log clients", logClientCount));

            int logClientOffset = restartAreaOffset + restartArea.getClientArrayOffset();
            LogClientRecord logClientRecord = new LogClientRecord(logFileBuffer, logClientOffset);
            logClients.add(logClientRecord);

            for (int i = 1; i <= logClientCount; i++) {
                logClientOffset = restartAreaOffset + logClientRecord.getNextClientOffset();
                logClientRecord = new LogClientRecord(logFileBuffer, logClientOffset);
                logClients.add(logClientRecord);
            }
        }

        oldestPageOffset = findOldestPageOffset();
    }

    /**
     * Parses the log records.
     */
    private void parseRecords() {
        if (lsnLogRecordMap != null) {
            // Already parsed
            return;
        }

        lsnLogRecordMap = new LinkedHashMap<Long, LogRecord>();

        // The first whole record in the oldest page can start mid-page, so just skip all records in the first page and
        // use the last record to calculate the offset to the first record in the next page.
        int offset = oldestPageOffset;
        RecordPageHeader oldestPage = offsetPageMap.get(offset);
        long recordOffset = oldestPage.getNextRecordOffset();
        recordOffset = FSUtils.roundUpToBoundary(8, recordOffset);
        LogRecord lastRecordOnFirstPage;
        long lastRecordLength;
        if (recordOffset + LogRecord.LENGTH_CALCULATION_OFFSET > logPageSize) {
            // The first record we hit has overflowed to the next page
            offset += logPageSize;
            recordOffset = restartArea.getLogPageDataOffset();
            lastRecordOnFirstPage = new LogRecord(logFileBuffer, (int) (offset + recordOffset), logPageSize,
                restartArea.getLogPageDataOffset());
            lastRecordLength = lastRecordOnFirstPage.getClientDataLength();
        } else {
            // Last record on this page with no overflow
            lastRecordOnFirstPage = new LogRecord(logFileBuffer, (int) (offset + recordOffset), logPageSize,
                restartArea.getLogPageDataOffset());
            lastRecordLength = lastRecordOnFirstPage.getClientDataLength();
            offset += logPageSize;
            lastRecordLength -= logPageSize - restartArea.getLogPageDataOffset();
        }

        recordOffset = getNextRecordOffset(lastRecordOnFirstPage, recordOffset);
        long lastLsn = offsetPageMap.get(offset).getLastLsnOrFileOffset();

        int logPageCount = (int) ((logFileLength - NORMAL_AREA_START * logPageSize) / logPageSize);

        // Read in each log page
        for (int pageNumber = 1; pageNumber < logPageCount; pageNumber++) {
            RecordPageHeader pageHeader = offsetPageMap.get(offset);

            if (pageHeader != null && pageHeader.isValid()) {
                if (pageHeader.getLastLsnOrFileOffset() < lastLsn ||
                    pageHeader.getLastLsnOrFileOffset() - lastLsn > 0x8000) {
                    // This page doesn't seem to continue on from the last page, so reset the offsets
                    log.info(String.format("$LogFile discontinuous at 0x%x [%d -> %d]", offset, lastLsn,
                        pageHeader.getLastLsnOrFileOffset()));
                    recordOffset = 0;
                    lastRecordLength = 0;
                }

                // Check if this page is filled with data from a previous record
                if (lastRecordLength > logPageSize - restartArea.getLogPageDataOffset()) {
                    recordOffset -= logPageSize - restartArea.getLogPageDataOffset();
                    lastRecordLength -= logPageSize - restartArea.getLogPageDataOffset();
                } else {
                    // Ensure that the record offset is within the page and beyond the page header
                    if (recordOffset < logPageSize) {
                        recordOffset = restartArea.getLogPageDataOffset();
                    } else {
                        recordOffset = recordOffset % logPageSize;
                        recordOffset += restartArea.getLogPageDataOffset();
                    }

                    long lastRecordInPage = Math.min(pageHeader.getNextRecordOffset(), logPageSize);
                    if (lastRecordInPage == 0) {
                        lastRecordInPage = logPageSize;
                    }

                    // Read in the page's log records
                    while (recordOffset <= lastRecordInPage) {
                        if (recordOffset + LogRecord.LENGTH_CALCULATION_OFFSET > logPageSize) {
                            // No more room for records in this page, move to the next page
                            recordOffset = 0;
                            break;
                        }

                        // Get the offset to the next record in the buffer rounded up to an 8-byte boundary
                        recordOffset = FSUtils.roundUpToBoundary(8, recordOffset);
                        LogRecord logRecord = new LogRecord(logFileBuffer, (int) (offset + recordOffset), logPageSize,
                            restartArea.getLogPageDataOffset());

                        recordOffset = getNextRecordOffset(logRecord, recordOffset);
                        long lsn = logRecord.getLsn();

                        if (logRecord.isValid() && lsn > 0 && lsn <= pageHeader.getLastLsnOrFileOffset()) {
                            lsnLogRecordMap.put(lsn, logRecord);
                            lastRecordLength = logRecord.getClientDataLength();

                            // Account for the portion of the record on this page
                            int start = (logRecord.getOffset()  % logPageSize) + LogRecord.LENGTH_CALCULATION_OFFSET;
                            lastRecordLength -= logPageSize - start;
                        } else {
                            if (lsn <= 0 || lsn > pageHeader.getLastLsnOrFileOffset()) {
                                log.warn("Log record seems to be invalid: " + logRecord.toDebugString());
                            }
                            // Seems to be the end of valid records for this page
                            lastRecordLength = 0;
                            break;
                        }
                    }
                }

                lastLsn = pageHeader.getLastLsnOrFileOffset();
            } else {
                lastLsn = 0;
            }

            offset += logPageSize;
            if (offset >= logFileLength) {
                // Wrap around to the start of the 'normal' area.
                offset = NORMAL_AREA_START * logPageSize;
            }
        }
    }

    /**
     * Gets the next record offset.
     *
     * @param logRecord the current log record.
     * @param recordOffset the offset to the current record.
     * @return the offset to the next record.
     */
    private long getNextRecordOffset(LogRecord logRecord, long recordOffset) {
        if (logRecord.isValid()) {
            return recordOffset + LogRecord.LENGTH_CALCULATION_OFFSET + (int) logRecord.getClientDataLength();
        } else {
            // Seems to be the end of valid records for this page
            return restartArea.getLogPageDataOffset();
        }
    }

    /**
     * Finds the offset to the oldest page, i.e. the one with the lowest LSN.
     *
     * @return the offset to the oldest page.
     * @throws IOException if an error occurs.
     */
    private int findOldestPageOffset() throws IOException {
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

            RecordPageHeader pageHeader = new RecordPageHeader(logFileBuffer, offset);
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
     * @throws IOException if an error occurs.
     */
    private RestartPageHeader getNewestRestartPageHeader(byte[] buffer) throws IOException {
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
    public Collection<LogRecord> getLogRecords() {
        parseRecords();
        return lsnLogRecordMap.values();
    }

    /**
     * Gets a mapping of LSN to log record.
     *
     * @return the map.
     */
    public Map<Long, LogRecord> getLsnLogRecordMap() {
        parseRecords();
        return Collections.unmodifiableMap(lsnLogRecordMap);
    }

    /**
     * Dumps out a chain of log records.
     *
     * @param lsn the LSN to start from.
     * @return the dumped out chain.
     */
    public String dumpLogChain(long lsn) {
        parseRecords();
        List<LogRecord> records = new ArrayList<LogRecord>();
        LogRecord midRecord = lsnLogRecordMap.get(lsn);
        records.add(midRecord);

        LogRecord current = midRecord;
        while (current.getClientPreviousLsn() != 0) {
            current = lsnLogRecordMap.get(current.getClientPreviousLsn());
            records.add(0, current);
        }

        int midIndex = records.size() - 1;
        current = midRecord;
        while (current.getClientUndoNextLsn() != 0) {
            current = lsnLogRecordMap.get(current.getClientUndoNextLsn());
            records.add(current);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < records.size(); i++) {
            LogRecord record = records.get(i);

            if (i < midIndex) {
                builder.append("<");
            } else if (i == midIndex) {
                builder.append("=");
            } else {
                builder.append(">");
            }

            builder.append(record);
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}
