package org.jnode.fs.xfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;
import org.jnode.fs.spi.AbstractFSDirectory;
import org.jnode.fs.spi.FSEntryTable;
import org.jnode.fs.xfs.inode.INode;
import org.jnode.fs.xfs.inode.ShortFormDirectoryEntry;
import org.jnode.util.BigEndian;

/**
 * A XFS directory.
 *
 * @author Luke Quinane.
 */
public class XfsDirectory extends AbstractFSDirectory implements FSDirectoryId {

    /**
     * The related entry.
     */
    private final XfsEntry entry;

    /**
     * The inode.
     */
    private final INode inode;

    /**
     * The file system.
     */
    private final XfsFileSystem fileSystem;

    /**
     * Creates a new directory.
     *
     * @param entry the entry.
     */
    public XfsDirectory(XfsEntry entry) {
        super((XfsFileSystem) entry.getFileSystem());

        this.entry = entry;
        fileSystem = (XfsFileSystem) entry.getFileSystem();
        inode = entry.getINode();
    }

    @Override
    public String getDirectoryId() {
        return Long.toString(inode.getINodeNr());
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        return null;
    }

    @Override
    protected FSEntryTable readEntries() throws IOException {
        List<FSEntry> entries = new ArrayList<FSEntry>();

        switch (inode.getFormat()) {
            case XfsConstants.XFS_DINODE_FMT_LOCAL:
                // Entries are stored within the inode record itself

                int fourByteEntries = BigEndian.getUInt8(inode.getData(), INode.DATA_OFFSET);
                int eightByteEntries = BigEndian.getUInt8(inode.getData(), INode.DATA_OFFSET + 1);
                int recordSize = fourByteEntries > 0 ? 4 : 8;
                int entryCount = fourByteEntries > 0 ? fourByteEntries : eightByteEntries;
                int offset = INode.DATA_OFFSET + inode.getOffset() + 0x6;

                while (entries.size() < entryCount) {
                    ShortFormDirectoryEntry entry = new ShortFormDirectoryEntry(inode.getData(), offset, recordSize);

                    if (entry.getINumber() == 0) {
                        break;
                    }

                    INode childInode = fileSystem.getInodeBTree().getINode(entry.getINumber());
                    entries.add(new XfsEntry(childInode, entry.getName(), entries.size(), fileSystem, this));

                    offset += entry.getLength();
                }

                break;

            case XfsConstants.XFS_DINODE_FMT_EXTENTS:
                /*
                The actual directory entries are located in another filesystem
                                block, the inode contains an array of extents to these filesystem blocks (xfs_bmbt_rec_t*).
                 */
                throw new UnsupportedOperationException();

            case XfsConstants.XFS_DINODE_FMT_BTREE:
                /*
                The directory entries are contained in the leaves of a B+tree. The
                inode contains the root node (xfs_bmdr_block_t*).
                 */
                throw new UnsupportedOperationException();

            default:
                throw new IllegalStateException("Unexpected format: " + inode.getFormat());
        }

        return new FSEntryTable(fileSystem, entries);
    }

    @Override
    protected void writeEntries(FSEntryTable entries) throws IOException {
        throw new UnsupportedOperationException("XFS is read-only");
    }

    @Override
    protected FSEntry createFileEntry(String name) throws IOException {
        throw new UnsupportedOperationException("XFS is read-only");
    }

    @Override
    protected FSEntry createDirectoryEntry(String name) throws IOException {
        throw new UnsupportedOperationException("XFS is read-only");
    }
}
