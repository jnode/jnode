package org.jnode.fs.xfs;

/**
 * XFS constants.
 *
 * @author Luke Quinane.
 */
public class XfsConstants {
    public static final int XFS_DINODE_FMT_DEV = 0;
    public static final int XFS_DINODE_FMT_LOCAL = 1;
    public static final int XFS_DINODE_FMT_EXTENTS = 2;
    public static final int XFS_DINODE_FMT_BTREE = 3;
    public static final int XFS_DINODE_FMT_UUID = 4;

    /**
     * Set if any inode have extended attributes.
     */
    public static final int XFS_SB_VERSION_ATTRBIT  = 0x10;

    /**
     * Set if any inodes use 32-bit di_nlink values.
     */
    public static final int XFS_SB_VERSION_NLINKBIT = 0x20;

    /**
     * Quotas are enabled on the filesystem. This also brings in the various quota fields in the superblock.
     */
    public static final int XFS_SB_VERSION_QUOTABIT = 0x40;

    /**
     * Set if sb_inoalignmt is used.
     */
    public static final int XFS_SB_VERSION_ALIGNBIT = 0x80;

    /**
     * Set if sb_unit and sb_width are used.
     */
    public static final int XFS_SB_VERSION_DALIGNBIT = 0x100;

    /**
     * Set if sb_shared_vn is used.
     */
    public static final int XFS_SB_VERSION_SHAREDBIT = 0x200;

    /**
     * Version 2 journaling logs are used.
     */
    public static final int XFS_SB_VERSION_LOGV2BIT = 0x400;

    /**
     * Set if sb_sectsize is not 512.
     */
    public static final int XFS_SB_VERSION_SECTORBIT = 0x800;

    /**
     * Unwritten extents are used. This is always set.
     */
    public static final int XFS_SB_VERSION_EXTFLGBIT = 0x1000;

    /**
     * Version 2 directories are used. This is always set.
     */
    public static final int XFS_SB_VERSION_DIRV2BIT = 0x2000;

    public static final int XFS_SB_VERSION_BORBBIT = 0x4000;

    /**
     * Set if the sb_features2 field in the superblock contains more flags.
     */
    public static final int XFS_SB_VERSION_MOREBITSBIT = 0x8000;


}
