package org.jnode.fs.xfs.btree;

import java.util.List;
import org.jnode.fs.xfs.XfsRecord;

/**
 * The b-tree header structure ('xfs_btree_sblock').
 *
 * @author Luke Quinane
 */
public abstract class BTreeHeader<T extends BTreeRecord> extends XfsRecord {

    /**
     * The length of the header structure.
     */
    public static final int LENGTH = 0x10;

    /**
     * Creates a new header record.
     *
     * @param data the data.
     */
    public BTreeHeader(byte[] data) {
        super(data, 0);
    }

    /**
     * Gets the current level in the b-tree.
     *
     * @return the level.
     */
    public int getLevel() {
        return getUInt16(0x4);
    }

    /**
     * Gets the record count.
     *
     * @return the record count.
     */
    public int getRecordCount() {
        return getUInt16(0x6);
    }

    /**
     * Gets the left sibling.
     *
     * @return the left sibling.
     */
    public long getLeftSib() {
        return getUInt32(0x8);
    }

    /**
     * Gets the right sibling.
     *
     * @return the right sibling.
     */
    public long getRightSib() {
        return getUInt32(0xc);
    }

    /**
     * Reads in the records.
     *
     * @return the records.
     */
    public abstract List<T> readRecords();

    @Override
    public String toString() {
        return String.format("btree-header:[level:%d records:%d left:%x right:%x]", getLevel(), getRecordCount(),
            getLeftSib(), getRightSib());
    }
}
