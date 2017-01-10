package org.jnode.fs.xfs.directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.xfs.XfsRecord;

/**
 * A directory data header ('xfs_dir2_data_hdr').
 *
 * @author Luke Quinane.
 */
public class DirectoryDataHeader extends XfsRecord {

    /**
     * The length of the header.
     */
    public static final int LENGTH = 0x10;

    /**
     * The magic number ('XD2B').
     */
    public static final long MAGIC = 0x58443242;

    /**
     * Creates a new directory data header.
     *
     * @param data the data.
     * @param offset the offset to read from.
     */
    public DirectoryDataHeader(byte[] data, int offset) throws IOException {
        super(data, offset);

        if (getMagic() != MAGIC) {
            throw new IOException("Wrong magic number for XD2B: " + getMagic());
        }
    }

    /**
     * Reads in the entries in this directory data record.
     *
     * @param blockSize the block size of the file system.
     * @return the list of entries.
     */
    public List<DirectoryDataEntry> readEntries(int blockSize) {

        // Read in the live entry offsets
        List<Integer> entryOffsets = new ArrayList<Integer>();
        int count = (int) getUInt32(blockSize - 0x8);
        for (int i = 0; i < count; i++) {
            int tailOffset = blockSize - ((count - i) * 8 + 0x8);

            long hash = getUInt32(tailOffset);
            int entryOffset = (int) getUInt32(tailOffset + 4);

            entryOffsets.add(entryOffset);
        }

        // Next read in the entries
        List<DirectoryDataEntry> entries = new ArrayList<DirectoryDataEntry>();
        for (int entryOffset : entryOffsets) {
            // The offsets are stored divided by 8 (XFS_DIR2_DATA_ALIGN)
            DirectoryDataEntry entry = new DirectoryDataEntry(getData(), entryOffset * 8);
            entries.add(entry);
        }

        return entries;
    }
}
