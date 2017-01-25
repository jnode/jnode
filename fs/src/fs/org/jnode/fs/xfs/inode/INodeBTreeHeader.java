package org.jnode.fs.xfs.inode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.xfs.btree.BTreeHeader;

/**
 * An inode b-tree header.
 *
 * @author Luke Quinane.
 */
public class INodeBTreeHeader extends BTreeHeader<INodeBTreeRecord> {

    /**
     * The magic number for the inode b-tree header ('IABT').
     */
    public static final long MAGIC = 0x49414254L;

    /**
     * The magic number for the inode b-tree CRC header ('IAB3').
     */
    public static final long MAGIC_CRC = 0x49414233L;

    /**
     * Creates a new header.
     *
     * @param data the data.
     */
    public INodeBTreeHeader(byte[] data) throws IOException {
        super(data);

        if (getMagic() == MAGIC_CRC) {

        } else if (getMagic() != MAGIC) {
            throw new IOException("Wrong magic number for inode b-tree header: " + getMagic());
        }
    }

    @Override
    public List<INodeBTreeRecord> readRecords() {
        List<INodeBTreeRecord> records = new ArrayList<INodeBTreeRecord>();
        int recordCount = getRecordCount();

        // Not sure what these extra bytes are for in the CRC case
        int offset = getMagic() == MAGIC_CRC ? 0x38 : LENGTH;

        for (; offset < getData().length && records.size() < recordCount;
             offset += INodeBTreeRecord.LENGTH) {

            records.add(new INodeBTreeRecord(getData(), offset));
        }

        return records;
    }
}
