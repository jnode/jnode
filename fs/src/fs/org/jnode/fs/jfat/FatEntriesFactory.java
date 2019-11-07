package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;

public class FatEntriesFactory implements Iterator<FatEntry> {

    private static final Logger log = Logger.getLogger(FatEntriesFactory.class);

    private boolean label;
    private int index;
    private FatEntry entry;

    /**
     * A flag indicating whether to include deleted entries.
     */
    protected boolean includeDeleted;

    /**
     * The parent directory.
     */
    private FatDirectory directory;

    public FatEntriesFactory(FatDirectory directory, boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        this.directory = directory;
    }

    @Override
    public boolean hasNext() {
        if (entry == null) {
            fetchNext();
        }

        return entry != null;
    }

    /**
     * Fetches the next entry into {@link #entry}.
     */
    protected void fetchNext() {
        if (index > FatDirectory.MAXENTRIES) {
            log.debug("Full Directory: invalid index " + index);
        }

        FatDirEntry dirEntry;
        FatRecord record = new FatRecord();
        int i = index;

        while (true) {
            try {
                // Read the next entry
                dirEntry = directory.getFatDirEntry(i, includeDeleted);
                i++;
            } catch (NoSuchElementException ex) {
                entry = null;
                return;
            } catch (IOException ex) {
                log.debug("cannot read entry " + i);
                i++;
                continue;
            }

            if (dirEntry.isFreeDirEntry() && dirEntry.isLongDirEntry() && includeDeleted) {
                // Ignore damage on deleted long directory entries
                ((FatLongDirEntry) dirEntry).setDamaged(false);
            }

            if (dirEntry.isFreeDirEntry() && !includeDeleted) {
                record.clear();
            } else if (dirEntry.isLongDirEntry()) {
                FatLongDirEntry longDirEntry = (FatLongDirEntry) dirEntry;
                if (longDirEntry.isDamaged()) {
                    log.debug("Damaged entry at " + (i - 1));
                    record.clear();
                } else {
                    record.add(longDirEntry);
                }
            } else if (dirEntry.isShortDirEntry()) {
                FatShortDirEntry shortDirEntry = (FatShortDirEntry) dirEntry;
                if (shortDirEntry.isLabel()) {
                    if (directory.isRoot()) {
                        FatRootDirectory root = (FatRootDirectory) directory;
                        if (label) {
                            log.debug("Duplicated label in root directory");
                        } else {
                            root.setEntry(shortDirEntry);
                            label = true;
                        }
                    } else {
                        log.debug("Volume label in non root directory");
                    }
                } else {
                    break;
                }
            } else if (dirEntry.isLastDirEntry()) {
                entry = null;
                return;
            } else {
                throw new UnsupportedOperationException("FatDirEntry is of unknown type, shouldn't happen");
            }
        }

        if (!dirEntry.isShortDirEntry()) {
            throw new UnsupportedOperationException("shouldn't happen");
        }

        record.close((FatShortDirEntry) dirEntry);

        if (((FatShortDirEntry) dirEntry).isDirectory()) {
            this.entry = createFatDirectory(record);
        } else {
            this.entry = createFatFile(record);
        }

        index = i;
    }

    /**
     * Creates a new FAT directory for the given record.
     *
     * @param record the record to create the directory from.
     * @return the directory.
     */
    protected FatEntry createFatDirectory(FatRecord record) {
        return new FatDirectory(directory.getFatFileSystem(), directory, record);
    }

    /**
     * Creates a new FAT file for the given record.
     *
     * @param record the record to create the file from.
     * @return the file.
     */
    protected FatEntry createFatFile(FatRecord record) {
        return new FatFile(directory.getFatFileSystem(), directory, record);
    }

    @Override
    public FatEntry next() {
        if (entry == null) {
            fetchNext();
        }

        if (entry == null) {
            throw new NoSuchElementException();
        }

        FatEntry result = entry;
        entry = null;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}