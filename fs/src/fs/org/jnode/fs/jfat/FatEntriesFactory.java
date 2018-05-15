package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;

public class FatEntriesFactory implements Iterator<FatEntry> {

    private static final Logger log = Logger.getLogger(FatEntriesFactory.class);

    private boolean label;
    private int index;
    private int next;
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
        label = false;
        index = 0;
        next = 0;
        entry = null;
        this.includeDeleted = includeDeleted;
        this.directory = directory;
    }

    /**
     * Returns the index of the entry the factory is up to.
     *
     * @return the index.
     */
    public int getIndex() {
        return index;
    }

    @Override
    public boolean hasNext() {
        int i;
        FatDirEntry dirEntry;
        FatRecord record = new FatRecord();

        if (index > FatDirectory.MAXENTRIES)
            log.debug("Full Directory: invalid index " + index);

        for (i = index;; ) {
                /*
                 * create a new entry from the chain
                 */
            try {
                dirEntry = directory.getFatDirEntry(i, includeDeleted);
                i++;
            } catch (NoSuchElementException ex) {
                entry = null;
                return false;
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
                return false;
            } else
                throw new UnsupportedOperationException(
                    "FatDirEntry is of unknown type, shouldn't happen");
        }

        if (!dirEntry.isShortDirEntry())
            throw new UnsupportedOperationException("shouldn't happen");

        record.close((FatShortDirEntry) dirEntry);

            /*
             * here recursion is in action for the entries factory it creates
             * directory nodes and file leafs
             */
        if (((FatShortDirEntry) dirEntry).isDirectory()) {
            this.entry = createFatDirectory(record);
        } else {
            this.entry = createFatFile(record);
        }

        this.next = i;

        return true;
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
        if (index == next) {
            hasNext();
        }

        if (entry == null) {
            throw new NoSuchElementException();
        }

        index = next;
        return entry;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}