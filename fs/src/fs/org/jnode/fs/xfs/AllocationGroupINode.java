package org.jnode.fs.xfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.FileSystemException;

/**
 * The allocation group for inodes ('xfs_agi').
 *
 * @author Luke Quinane.
 */
public class AllocationGroupINode extends XfsRecord {

    /**
     * The length of this record.
     */
    private static final int LENGTH = 0x200;

    /**
     * The offset to this record.
     */
    private static final int OFFSET = 0x400;

    /**
     * The magic number ('XAGI').
     */
    public static final long MAGIC = 0x58414749;

    /**
     * Creates a new allocation group for inodes.
     *
     * @param fileSystem the file system.
     * @throws FileSystemException if an error occurs.
     */
    public AllocationGroupINode(XfsFileSystem fileSystem) throws FileSystemException {
        super(new byte[LENGTH], 0);

        try {
            ByteBuffer buffer = ByteBuffer.allocate(LENGTH);
            fileSystem.getApi().read(OFFSET, buffer);
            buffer.position(0);
            buffer.get(getData());

            if (getMagic() != MAGIC) {
                throw new FileSystemException("Wrong magic number for XAGI: " + getMagic());
            }
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Gets the AG number for the sector.
     *
     * @return the the AG number for the sector.
     */
    public long getSeqNo() {
        return getUInt32(0x8);
    }

    /**
     * Gets the length of the allocation group in blocks.
     *
     * @return the length in blocks.
     */
    public long getLength() {
        return getUInt32(0xc);
    }

    /**
     * Gets the number of inodes allocated for the AGI.
     *
     * @return the number of inodes.
     */
    public long getCount() {
        return getUInt32(0x10);
    }

    /**
     * Gets the root block number.
     *
     * @return the root block number.
     */
    public long getRootBlock() {
        return getUInt32(0x14);
    }

    /**
     * Gets the number of levels in the b-tree.
     *
     * @return the number of levels.
     */
    public long getLevel() {
        return getUInt32(0x18);
    }

    @Override
    public String toString() {
        return String.format("AI-inode:[seqno:0x%x length:%d count:%d root-block:0x%x levels:%d]",
            getSeqNo(), getLength(), getCount(), getRootBlock(), getLevel());
    }
}
