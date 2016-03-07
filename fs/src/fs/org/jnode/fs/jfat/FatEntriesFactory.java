package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;

public class FatEntriesFactory {

    private static final Logger log = Logger.getLogger(FatEntriesFactory.class);

    private boolean label;
    private int index;
    private int next;
    private FatEntry entry;
    protected boolean includeDeleted;
    private FatDirectory directory;

    protected FatEntriesFactory(FatDirectory directory, boolean includeDeleted) {
        label = false;
        index = 0;
        next = 0;
        entry = null;
        this.includeDeleted = includeDeleted;
        this.directory = directory;
    }

    protected boolean hasNextEntry() {
        int i;
        FatDirEntry e;
        FatRecord v = new FatRecord();

        if (index > FatDirectory.MAXENTRIES)
            log.debug("Full Directory: invalid index " + index);

        for (i = index;; ) {
                /*
                 * create a new entry from the chain
                 */
            try {
                e = directory.getFatDirEntry(i, includeDeleted);
                i++;
            } catch (NoSuchElementException ex) {
                entry = null;
                return false;
            } catch (IOException ex) {
                log.debug("cannot read entry " + i);
                i++;
                continue;
            }

            if (e.isFreeDirEntry() && e.isLongDirEntry() && includeDeleted) {
                // Ignore damage on deleted long directory entries
                ((FatLongDirEntry) e).setDamaged(false);
            }

            if (e.isFreeDirEntry() && !includeDeleted) {
                v.clear();
            } else if (e.isLongDirEntry()) {
                FatLongDirEntry l = (FatLongDirEntry) e;
                if (l.isDamaged()) {
                    log.debug("Damaged entry at " + (i - 1));
                    v.clear();
                } else {
                    v.add(l);
                }
            } else if (e.isShortDirEntry()) {
                FatShortDirEntry s = (FatShortDirEntry) e;
                if (s.isLabel()) {
                    if (directory.isRoot()) {
                        FatRootDirectory r = (FatRootDirectory) directory;
                        if (label) {
                            log.debug("Duplicated label in root directory");
                        } else {
                            r.setEntry(s);
                            label = true;
                        }
                    } else {
                        log.debug("Volume label in non root directory");
                    }
                } else {
                    break;
                }
            } else if (e.isLastDirEntry()) {
                entry = null;
                return false;
            } else
                throw new UnsupportedOperationException(
                    "FatDirEntry is of unknown type, shouldn't happen");
        }

        if (!e.isShortDirEntry())
            throw new UnsupportedOperationException("shouldn't happen");

        v.close((FatShortDirEntry) e);

            /*
             * here recursion is in action for the entries factory it creates
             * directory nodes and file leafs
             */
        if (((FatShortDirEntry) e).isDirectory())
            this.entry = new FatDirectory(directory.getFatFileSystem(), directory, v);
        else
            this.entry = new FatFile(directory.getFatFileSystem(), directory, v);

        this.next = i;

        return true;
    }

    protected FatEntry createNextEntry() {
        if (index == next)
            hasNextEntry();
        if (entry == null)
            throw new NoSuchElementException();
        index = next;
        return entry;
    }
}