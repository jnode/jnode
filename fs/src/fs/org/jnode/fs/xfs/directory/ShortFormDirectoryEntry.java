package org.jnode.fs.xfs.directory;

import java.io.UnsupportedEncodingException;
import org.jnode.fs.xfs.XfsObject;

/**
 * A short form directory entry ('xfs_dir2_sf_entry_t').
 *
 * @author Luke Quinane
 */
public class ShortFormDirectoryEntry extends XfsObject {

    /**
     * The size of inode entries in this directory (4 or 8 bytes).
     */
    private int inodeSize;

    /**
     * Creates a new short-form directory entry.
     *
     * @param data the data.
     * @param offset the offset.
     * @param inodeSize the size of inode entries in this directory (4 or 8 bytes).
     */
    public ShortFormDirectoryEntry(byte[] data, int offset, int inodeSize) {
        super(data, offset);
        this.inodeSize = inodeSize;
    }

    /**
     * Gets the length of the name.
     *
     * @return the length.
     */
    public int getNameLength() {
        return getUInt8(0);
    }

    /**
     * Gets the directory entry offset.
     *
     * @return the offset.
     */
    public int getDirectoryEntryOffset() {
        return getUInt16(1);
    }

    /**
     * Gets the name of the entry.
     *
     * @return the entry name.
     */
    public String getName() {
        try {
            return new String(getData(), getOffset() + 0x3, getNameLength(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Error reading name bytes", e);
        }
    }

    /**
     * Gets the inode number.
     *
     * @return the inode number.
     */
    public long getINumber() {
        int numberOffset = getNameLength() + 0x3;
        return inodeSize == 4 ? getUInt32(numberOffset) : getInt64(numberOffset);
    }

    /**
     * Get the length for this entry.
     *
     * @return the entry length.
     */
    public int getLength() {
        return getNameLength() + 0x7;
    }

    @Override
    public String toString() {
        return String.format("short-dir-entry:[inum: %d name:%s]", getINumber(), getName());
    }
}
