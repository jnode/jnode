package org.jnode.fs.xfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;

/**
 * The XFS superblock ('xfs_sb').
 *
 * @author Luke Quinane
 */
public class Superblock extends XfsRecord {

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(Superblock.class);

    /**
     * The size of the super block.
     */
    public static final int SUPERBLOCK_LENGTH = 512;

    /**
     * The super block magic number ('XFSB').
     */
    public static final long XFS_SUPER_MAGIC = 0x58465342;

    /**
     * Creates a new super block.
     *
     * @param fileSystem the file system to read from.
     * @throws FileSystemException if an error occurs reading in the super block.
     */
    public Superblock(XfsFileSystem fileSystem) throws FileSystemException {
        super(new byte[SUPERBLOCK_LENGTH], 0);

        try {
            log.debug("Reading XFS super block");

            ByteBuffer buffer = ByteBuffer.allocate(SUPERBLOCK_LENGTH);
            fileSystem.getApi().read(0, buffer);
            buffer.position(0);
            buffer.get(getData());

            if (getMagic() != XFS_SUPER_MAGIC) {
                throw new FileSystemException("Wrong magic number for XFS: " + getMagic());
            }
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Gets the block size stored in the super block.
     *
     * @return the block size.
     */
    public int getBlockSize() {
        return (int) getUInt32(0x4);
    }

    /**
     * Gets the UUID for the volume stored in the super block.
     *
     * @return the UUID.
     */
    public byte[] getUuid() {
        byte[] uuid = new byte[16];
        System.arraycopy(getData(), 0x20, uuid, 0, uuid.length);
        return uuid;
    }

    /**
     * Gets the root inode.
     *
     * @return the root inode.
     */
    public long getRootInode() {
        return getInt64(0x38);
    }

    /**
     * Gets the size of each allocation group in blocks.
     *
     * @return the size in blocks.
     */
    public long getAGSize() {
        return getUInt32(0x54);
    }

    /**
     * Gets the number of allocation groups in the file system.
     *
     * @return the number of allocation groups.
     */
    public long getAGCount() {
        return getUInt32(0x58);
    }

    /**
     * Gets the file system features (sb_versionnum).
     *
     * @return the features.
     */
    public int getVersion() {
        return getUInt16(0x64);
    }

    /**
     * Gets the size of inodes in the file system.
     *
     * @return the inode size.
     */
    public int getInodeSize() {
        return getUInt16(0x68);
    }

    /**
     * Gets the number of inodes per block (should be the same as block size / inode size).
     *
     * @return the inodes per block.
     */
    public int getInodesPerBlock() {
        return getUInt16(0x6a);
    }

    /**
     * Gets the name for the file system.
     *
     * @return the name.
     */
    public String getName() {
        byte[] buffer = new byte[12];
        System.arraycopy(getData(), getOffset() + 0x6c, buffer, 0, buffer.length);
        return new String(buffer, UTF8);
    }

    /**
     * Gets the number of inodes currently allocation in the file system.
     *
     * @return the number of allocated inodes.
     */
    public long getInodeCount() {
        return getInt64(0x80);
    }

    /**
     * Gets the number of inodes currently allocation in the file system.
     *
     * @return the number of allocated inodes.
     */
    public long getInodeAlignment() {
        return getUInt32(0xb4);
    }

    /**
     * Gets additional features.
     *
     * @return the additional features.
     */
    public long getFeatures2() {
        return getUInt32(0xc8);
    }

    /*
    typedef struct xfs_sb
    {
     ...
     __uint64_t sb_icount;     // 0x80
     __uint64_t sb_ifree;      // 0x88
     __uint64_t sb_fdblocks;   // 0x90
     __uint64_t sb_frextents;  // 0x98
     xfs_ino_t sb_uquotino;    // 0xa0
     xfs_ino_t sb_gquotino;    // 0xa8
     __uint16_t sb_qflags;     // 0xb0
     __uint8_t sb_flags;       // 0xb2
     __uint8_t sb_shared_vn;   // 0xb3
     xfs_extlen_t sb_inoalignmt; // 0xb4
     __uint32_t sb_unit;       // 0xb8
     __uint32_t sb_width;      // 0xbc
     __uint8_t sb_dirblklog;   // 0xc0
     __uint8_t sb_logsectlog;  // 0xc1
     __uint16_t sb_logsectsize;// 0xc2
     __uint32_t sb_logsunit;   // 0xc4
     __uint32_t sb_features2;  // 0xc8
    } xfs_sb_t;
     */

    /**
     * Checks whether the file system is using the given feature.
     *
     * @return {@code true} if using the feature.
     */
    public boolean hasFeature(int feature) {
        return (getVersion() & feature) == feature;
    }

    /**
     * Checks whether the file system is using the extended feature bits in {@link #getFeatures2()}.
     *
     * @return {@code true} if using extended features.
     */
    public boolean isUsingExtendedFeatures() {
        return hasFeature(XfsConstants.XFS_SB_VERSION_MOREBITSBIT);
    }

    @Override
    public String toString()
    {
        return String.format(
            "xfs-sb:[block-size:%d inode-size:%d root-ino:%d ag-size:%d ag-count: %d version:%d features2:0x%x]",
            getBlockSize(), getInodeSize(), getRootInode(), getAGSize(), getAGCount(), getVersion(), getFeatures2());
    }
}
