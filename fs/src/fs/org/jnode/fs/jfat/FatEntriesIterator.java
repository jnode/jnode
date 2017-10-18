package org.jnode.fs.jfat;

import java.util.Iterator;
import org.jnode.fs.FSEntry;

public class FatEntriesIterator implements Iterator<FSEntry> {

    private final FatTable fatTable;

    /**
     * The entries factory to read from.
     */
    private final FatEntriesFactory entriesFactory;

    /**
     * A flag indicating whether to include deleted entries in the iterator.
     */
    private final boolean includeDeleted;

    public FatEntriesIterator(FatTable fatTable, FatEntriesFactory entriesFactory, boolean includeDeleted) {
        this.entriesFactory = entriesFactory;
        this.includeDeleted = includeDeleted;
        this.fatTable = fatTable;
    }

    public boolean hasNext() {
        return entriesFactory.hasNextEntry();
    }

    public FSEntry next() {
        if (includeDeleted) {
            return entriesFactory.createNextEntry();
        } else {
            return fatTable.look(entriesFactory.createNextEntry());
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}