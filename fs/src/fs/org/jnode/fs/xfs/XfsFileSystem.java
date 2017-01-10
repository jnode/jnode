package org.jnode.fs.xfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.spi.AbstractFileSystem;
import org.jnode.fs.xfs.inode.INodeBTree;

/**
 * An XFS file system.
 *
 * @author Luke Quinane
 */
public class XfsFileSystem extends AbstractFileSystem<XfsEntry> {

    /**
     * The superblock.
     */
    private Superblock superblock;

    /**
     * The allocation group for inodes.
     */
    private AllocationGroupINode agINode;

    /**
     * The inode b-tree.
     */
    private INodeBTree inodeBTree;

    /**
     * Construct an XFS file system.
     *
     * @param device device contains file system.
     * @param type the file system type.
     * @throws FileSystemException device is null or device has no {@link BlockDeviceAPI} defined.
     */
    public XfsFileSystem(Device device, FileSystemType<? extends FileSystem<XfsEntry>> type)
        throws FileSystemException {

        super(device, true, type);
    }

    /**
     * Reads in the file system from the block device.
     *
     * @throws FileSystemException if an error occurs reading the file system.
     */
    public final void read() throws FileSystemException {
        superblock = new Superblock(this);
        agINode = new AllocationGroupINode(this);

        try {
            inodeBTree = new INodeBTree(this, agINode);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public long getTotalSpace() throws IOException {
        return 0;
    }

    @Override
    public long getFreeSpace() throws IOException {
        return 0;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return 0;
    }

    @Override
    public String getVolumeName() throws IOException {
        return null;
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        return new XfsFile((XfsEntry) entry);
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        return new XfsDirectory((XfsEntry) entry);
    }

    @Override
    protected XfsEntry createRootEntry() throws IOException {
        long rootIno = superblock.getRootInode();
        return new XfsEntry(inodeBTree.getINode(rootIno), "/", 0, this, null);
    }

    /**
     * Reads block from the file system.
     *
     * @param startBlock the start block.
     * @param dest the destination to read into.
     * @throws IOException if an error occurs.
     */
    public void readBlocks(long startBlock, ByteBuffer dest) throws IOException {
        readBlocks(startBlock, 0, dest);
    }

    /**
     * Reads block from the file system.
     *
     * @param startBlock the start block.
     * @param blockOffset the offset within the block to start reading from.
     * @param dest the destination to read into.
     * @throws IOException if an error occurs.
     */
    public void readBlocks(long startBlock, int blockOffset, ByteBuffer dest) throws IOException {
        getApi().read(superblock.getBlockSize() * startBlock + blockOffset, dest);
    }

    /**
     * Gets the superblock.
     *
     * @return the superblock.
     */
    public Superblock getSuperblock() {
        return superblock;
    }

    /**
     * Gets the inode b-tree.
     *
     * @return the b-tree.
     */
    public INodeBTree getInodeBTree() {
        return inodeBTree;
    }
}
