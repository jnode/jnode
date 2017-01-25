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
     * The block offset to the root inode.
     */
    private static final int ROOT_INODE_BLOCK = 0x8;

    /**
     * The number of inodes per-chunk.
     */
    public static final int INODE_CHUNK_COUNT = 64;

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

        ByteBuffer buffer = ByteBuffer.allocate(fileSystem.getSuperblock().getBlockSize());
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
        int chunkNumber;
        boolean foundMatch = false;

        for (chunkNumber = 0; chunkNumber < records.size(); chunkNumber++) {
            INodeBTreeRecord record = records.get(chunkNumber);

            if (record.containsInode(inode)) {
                // Matching block...
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            throw new IOException("Failed to find an inode for: " + inode);
        }

        int blockSize = fileSystem.getSuperblock().getBlockSize();
        int inodeSize = fileSystem.getSuperblock().getInodeSize();
        int chunkSize = inodeSize * INODE_CHUNK_COUNT;
        int offset = (int) ((inode % INODE_CHUNK_COUNT) * inodeSize);


        byte[] data = new byte[chunkSize];

        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        int rootInodeOffset = ROOT_INODE_BLOCK * blockSize;
        fileSystem.getApi().read(rootInodeOffset + chunkNumber * chunkSize, buffer);
        buffer.position(0);
        buffer.get(data);


        return new INode(inode, data, offset);
    }
}
