package org.jnode.fs.xfs.inode;

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
     * Creates a new header.
     *
     * @param data the data.
     */
    public INodeBTreeHeader(byte[] data) {
        super(data);
    }

    @Override
    public List<INodeBTreeRecord> readRecords() {
        List<INodeBTreeRecord> records = new ArrayList<INodeBTreeRecord>();
        int recordCount = getRecordCount();

        for (int offset = LENGTH; offset < getData().length && records.size() < recordCount;
             offset += INodeBTreeRecord.LENGTH) {

            records.add(new INodeBTreeRecord(getData(), offset));
        }

        return records;
    }
}
