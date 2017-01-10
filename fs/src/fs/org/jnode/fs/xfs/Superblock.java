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
     * Gets the size of inodes in the file system.
     *
     * @return the inode size.
     */
    public int getInodeSize() {
        return getUInt16(0x68);
    }


    /*
    typedef struct xfs_sb
    {
     __uint32_t sb_magicnum;
     __uint32_t sb_blocksize;
     xfs_drfsbno_t sb_dblocks;
     xfs_drfsbno_t sb_rblocks;
     xfs_drtbno_t sb_rextents;
     uuid_t sb_uuid;
     xfs_dfsbno_t sb_logstart;
     xfs_ino_t sb_rootino;          // 0x38
     xfs_ino_t sb_rbmino;           // 0x40
     xfs_ino_t sb_rsumino;          // 0x48
     xfs_agblock_t sb_rextsize;
     xfs_agblock_t sb_agblocks;
     xfs_agnumber_t sb_agcount;
     xfs_extlen_t sb_rbmblocks;
     xfs_extlen_t sb_logblocks;
     __uint16_t sb_versionnum;
     __uint16_t sb_sectsize;
     __uint16_t sb_inodesize;     // 0x68
     __uint16_t sb_inopblock;
     char sb_fname[12];
     __uint8_t sb_blocklog;
     __uint8_t sb_sectlog;
     __uint8_t sb_inodelog;
     __uint8_t sb_inopblog;
     __uint8_t sb_agblklog;
     __uint8_t sb_rextslog;
     __uint8_t sb_inprogress;
     __uint8_t sb_imax_pct;
     __uint64_t sb_icount;
     __uint64_t sb_ifree;
     __uint64_t sb_fdblocks;
     __uint64_t sb_frextents;
     xfs_ino_t sb_uquotino;
     xfs_ino_t sb_gquotino;
     __uint16_t sb_qflags;
     __uint8_t sb_flags;
     __uint8_t sb_shared_vn;
     xfs_extlen_t sb_inoalignmt;
     __uint32_t sb_unit;
     __uint32_t sb_width;
     __uint8_t sb_dirblklog;
     __uint8_t sb_logsectlog;
     __uint16_t sb_logsectsize;
     __uint32_t sb_logsunit;
     __uint32_t sb_features2;
    } xfs_sb_t;
     */
}
