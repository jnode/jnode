package org.jnode.fs.xfs.inode;

import org.jnode.fs.xfs.XfsObject;
import org.jnode.fs.xfs.btree.BTreeRecord;

/**
 * An inode b-tree record ('xfs_inobt_rec').
 *
 * @author Luke Quinane.
 */
public class INodeBTreeRecord extends XfsObject implements BTreeRecord {

    /**
     * The length of inode b-tree records.
     */
    public static final int LENGTH = 0x10;

    /**
     * Creates a new inode b-tree record.
     *
     * @param data the data.
     * @param offset the offset to this record.
     */
    public INodeBTreeRecord(byte[] data, int offset) {
        super(data, offset);
    }

    /**
     * Gets the start inode for this block of inodes.
     *
     * @return the start inode number.
     */
    public long getStartIno() {
        return getUInt32(0);
    }

    /**
     * Gets the number of free inodes.
     *
     * @return the free count.
     */
    public long getFreeCount() {
        return getUInt32(0x4);
    }

    /**
     * Gets the free bitmask.
     *
     * @return the bitmask.
     */
    public long getFree() {
        return getInt64(0x8);
    }
}
