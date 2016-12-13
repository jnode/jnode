package org.jnode.fs.xfs.inode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.jnode.fs.xfs.AllocationGroupINode;
import org.jnode.fs.xfs.XfsFileSystem;

/**
 * A b-tree for inodes.
 *
 * @author Luke Quinane.
 */
public class INodeBTree {

    /**
     * The offset to the root inodes.
     */
    private static final int ROOT_INODES_OFFSET = 0x8000;

    /**
     * The root header record.
     */
    private INodeBTreeHeader header;

    /**
     * The file system.
     */
    private XfsFileSystem fileSystem;

    /**
     * Creates a new b-tree.
     *
     * @param fileSystem the file system.
     * @param agINode the inode allocation group.
     * @throws IOException if an error occurs.
     */
    public INodeBTree(XfsFileSystem fileSystem, AllocationGroupINode agINode) throws IOException {
        this.fileSystem = fileSystem;

        ByteBuffer buffer = ByteBuffer.allocate((int) fileSystem.getSuperblock().getBlockSize());
        fileSystem.readBlocks(agINode.getRootBlock(), buffer);
        header = new INodeBTreeHeader(buffer.array());
    }

    /**
     * Looks up an inode in the b-tree.
     *
     * @param inode the number of the inode to look up.
     * @return the inode.
     * @throws IOException if an error occurs.
     */
    public INode getINode(long inode) throws IOException {
        List<INodeBTreeRecord> records = header.readRecords();
        for (int i = 0; i < records.size(); i++) {
            INodeBTreeRecord record = records.get(i);
            long startInode = record.getStartIno();

            if (startInode + 64 < inode) {
                // Matching block...
                // TODO: use this block
            }

        }

        int inodeSize = fileSystem.getSuperblock().getInodeSize();
        int offset = (int) ((inode - 128) * inodeSize);

        byte[] data = new byte[(int) fileSystem.getSuperblock().getBlockSize()];

        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        fileSystem.getApi().read(ROOT_INODES_OFFSET, buffer);
        buffer.position(0);
        buffer.get(data);


        return new INode(inode, data, offset);
    }
}
