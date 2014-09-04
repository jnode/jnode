package org.jnode.fs.ntfs.index;

/**
 * The interface for an NTFS index search callback.
 *
 * @author Luke Quinane
 */
public interface IndexSearchCallback {

    /**
     * Visits the given NTFS index entry.
     *
     * @param entry the entry to visit.
     * @return {@code 0} if the entry matches the item being searched for, {@code -1} if the item occurs before this
     * item, {@code 1} if the item occurs after this item.
     */
    int visitAndCompareEntry(IndexEntry entry);
}
