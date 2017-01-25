package org.jnode.fs.xfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.util.UnixFSConstants;
import org.jnode.fs.xfs.extent.DataExtent;
import org.jnode.fs.xfs.inode.INode;

/**
 * An entry in a XFS file system.
 *
 * @author Luke Quinane
 */
public class XfsEntry extends AbstractFSEntry implements FSEntryCreated, FSEntryLastAccessed {

    /**
     * The inode.
     */
    private final INode inode;

    /**
     * The directory record ID.
     */
    private final long directoryRecordId;

    /**
     * The file system.
     */
    private final XfsFileSystem fileSystem;

    /**
     * The list of extents when the data format is 'XFS_DINODE_FMT_EXTENTS'.
     */
    private List<DataExtent> extentList;

    /**
     * Creates a new entry.
     *
     * @param inode the inode.
     * @param name the name.
     * @param directoryRecordId the directory record ID.
     * @param fileSystem the file system.
     * @param parent the parent.
     */
    public XfsEntry(INode inode, String name, long directoryRecordId, XfsFileSystem fileSystem, FSDirectory parent) {
        super(fileSystem, null, parent, name, getFSEntryType(name, inode));

        this.inode = inode;
        this.directoryRecordId = directoryRecordId;
        this.fileSystem = fileSystem;
    }

    @Override
    public String getId() {
        return Long.toString(inode.getINodeNr()) + '-' + Long.toString(directoryRecordId);
    }

    @Override
    public long getCreated() throws IOException {
        return inode.getCreatedTime();
    }

    @Override
    public long getLastAccessed() throws IOException {
        return inode.getAccessTime();
    }

    @Override
    public long getLastModified() throws IOException {
        return inode.getModifiedTime();
    }

    /**
     * Gets the inode.
     *
     * @return the inode.
     */
    public INode getINode() {
        return inode;
    }

    /**
     * Reads from this entry's data.
     *
     * @param offset the offset to read from.
     * @param destBuf the destination buffer.
     * @throws IOException if an error occurs reading.
     */
    public void read(long offset, ByteBuffer destBuf) throws IOException {
        if (offset + destBuf.remaining() > inode.getSize()) {
            throw new IOException("Reading past the end of the entry. Offset: " + offset + " entry: " + this);
        }

        readUnchecked(offset, destBuf);
    }

    /**
     * A read implementation that doesn't check the file length.
     *
     * @param offset the offset to read from.
     * @param destBuf the destination buffer.
     * @throws IOException if an error occurs reading.
     */
    public void readUnchecked(long offset, ByteBuffer destBuf) throws IOException {
        switch (inode.getFormat()) {
            case XfsConstants.XFS_DINODE_FMT_LOCAL:
                throw new UnsupportedOperationException();

            case XfsConstants.XFS_DINODE_FMT_EXTENTS:
                if (extentList == null) {
                    extentList = new ArrayList<DataExtent>();

                    for (int i = 0; i < inode.getExtentCount(); i++) {
                        int inodeDataOffset = inode.getVersion() >= 3 ? INode.V3_DATA_OFFSET : INode.DATA_OFFSET;
                        int inodeOffset = inode.getOffset() + inodeDataOffset;
                        int extentOffset = inodeOffset + i * DataExtent.PACKED_LENGTH;
                        DataExtent extent = new DataExtent(inode.getData(), extentOffset);
                        extentList.add(extent);
                    }
                }

                readFromExtentList(offset, destBuf);
                return;

            case XfsConstants.XFS_DINODE_FMT_BTREE:
                throw new UnsupportedOperationException();

            default:
                throw new IllegalStateException("Unexpected format: " + inode.getFormat());
        }
    }

    /**
     * Reads from the entry's extent list.
     *
     * @param offset the offset to read from.
     * @param destBuf the destination buffer.
     * @throws IOException if an error occurs reading.
     */
    private void readFromExtentList(long offset, ByteBuffer destBuf) throws IOException {
        int blockSize = fileSystem.getSuperblock().getBlockSize();
        long extentOffset = 0;

        for (DataExtent extent : extentList) {
            if (!destBuf.hasRemaining()) {
                return;
            }

            if (extent.isWithinExtent(offset, blockSize)) {
                ByteBuffer readBuffer = destBuf.duplicate();

                long blockOffset = (offset - extentOffset) / blockSize;
                int offsetWithinBlock = (int) (offset % blockSize);
                int bytesToRead = (int) Math.min(extent.getBlockCount() * blockSize, destBuf.remaining());
                readBuffer.limit(readBuffer.position() + bytesToRead);

                fileSystem.readBlocks(extent.getStartBlock() + blockOffset, offsetWithinBlock, readBuffer);

                offset += bytesToRead;
                destBuf.position(destBuf.position() + bytesToRead);
            }

            long extentLength = extent.getBlockCount() * blockSize;
            extentOffset += extentLength;
        }
    }

    @Override
    public String toString() {
        return "xfs-entry:[" + getName() + "] " + inode;
    }

    private static int getFSEntryType(String name, INode inode) {
        int mode = inode.getMode() & UnixFSConstants.S_IFMT;

        if ("/".equals(name))
            return AbstractFSEntry.ROOT_ENTRY;
        else if (mode == UnixFSConstants.S_IFDIR)
            return AbstractFSEntry.DIR_ENTRY;
        else if (mode == UnixFSConstants.S_IFREG || mode == UnixFSConstants.S_IFLNK ||
            mode == UnixFSConstants.S_IFIFO || mode == UnixFSConstants.S_IFCHR ||
            mode == UnixFSConstants.S_IFBLK)
            return AbstractFSEntry.FILE_ENTRY;
        else
            return AbstractFSEntry.OTHER_ENTRY;
    }
}
