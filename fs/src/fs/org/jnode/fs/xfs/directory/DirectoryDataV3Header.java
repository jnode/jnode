package org.jnode.fs.xfs.directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.xfs.XfsRecord;

/**
 * A directory data header ('xfs_dir3_data_hdr').
  *
  * @author Luke Quinane.
 */
public class DirectoryDataV3Header extends XfsRecord {

    /**
     * The length of the header.
     */
    public static final int LENGTH = 0x30;

    /**
     * The magic number ('XDD3').
     */
    public static final long MAGIC = 0x58444433;

    /**
     * Creates a new directory data header.
     *
     * @param data the data.
     * @param offset the offset to read from.
     */
    public DirectoryDataV3Header(byte[] data, int offset) throws IOException {
        super(data, offset);

        if (getMagic() != MAGIC) {
            throw new IOException("Wrong magic number for XDD3: " + getMagic());
        }
    }

    /**
     * Gets the block number for the first block in the buffer.
     *
     * @return the block number.
     */
    public long getBlockNumber() {
        return getInt64(0x8);
    }

    /**
     * Gets the UUID for the file system which owns this record.
     *
     * @return the UUID.
     */
    public byte[] getUuid() {
        byte[] uuid = new byte[16];
        System.arraycopy(getData(), getOffset() + 0x10, uuid, 0, uuid.length);
        return uuid;
    }

    /**
     * Gets the inode number which owns this directory list.
     *
     * @return the inode number.
     */
    public long getOwnerInode() {
        return getInt64(0x20);
    }

    /**
     * Reads in the entries in this directory data record.
     *
     * @param blockSize the block size of the file system.
     * @return the list of entries.
     */
    public List<DirectoryDataEntry> readEntries(int blockSize) {

        List<DirectoryDataEntry> entries = new ArrayList<DirectoryDataEntry>();

        // TODO: implement

        return entries;
    }

}
