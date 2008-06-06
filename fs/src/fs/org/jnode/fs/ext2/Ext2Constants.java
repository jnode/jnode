/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.ext2;

/**
 * @author Andras Nagy
 */
public class Ext2Constants {
    // file types that are stored in the directory records
    public static final int EXT2_FT_UNKNOWN = 0;
    public static final int EXT2_FT_REG_FILE = 1;
    public static final int EXT2_FT_DIR = 2;
    public static final int EXT2_FT_CHRDEV = 3;
    public static final int EXT2_FT_BLKDEV = 4;
    public static final int EXT2_FT_FIFO = 5;
    public static final int EXT2_FT_SOCK = 6;
    public static final int EXT2_FT_SYMLINK = 7;
    public static final int EXT2_FT_MAX = 8;

    // inode constants
    public static final int EXT2_BAD_INO = 0x01; // bad blocks inode
    public static final int EXT2_ROOT_INO = 0x02; // root directory inode
    public static final int EXT2_ACL_IDX_INO = 0x03; // ACL index node
    public static final int EXT2_ACL_DATA_INO = 0x04; // ACL data inode
    public static final int EXT2_BOOT_LOADER_INO = 0x05; // boot loader inode
    public static final int EXT2_UNDEL_DIR_INO = 0x06; // undelete directory inode

    // i_mode masks and values
    public static final int EXT2_S_IFMT = 0xF000; // format mask
    public static final int EXT2_S_IFSOCK = 0xC000; // socket
    public static final int EXT2_S_IFLNK = 0xA000; // symbolic link
    public static final int EXT2_S_IFREG = 0x8000; // regular file
    public static final int EXT2_S_IFBLK = 0x6000; // block device
    public static final int EXT2_S_IFDIR = 0x4000; // directory
    public static final int EXT2_S_IFCHR = 0x2000; // character device
    public static final int EXT2_S_IFIFO = 0x1000; // fifo
    
    // access rights
    public static final int EXT2_S_ISUID = 0x0800; // SUID
    public static final int EXT2_S_ISGID = 0x0400; // SGID
    public static final int EXT2_S_ISVTX = 0x0200; // sticky bit
    public static final int EXT2_S_IRWXU = 0x01C0; // user access right mask
    public static final int EXT2_S_IRUSR = 0x0100; // read
    public static final int EXT2_S_IWUSR = 0x0080; // write
    public static final int EXT2_S_IXUSR = 0x0040; // execute
    public static final int EXT2_S_IRWXG = 0x0038; // group access right mask
    public static final int EXT2_S_IRGRP = 0x0020; // read
    public static final int EXT2_S_IWGRP = 0x0010; // write
    public static final int EXT2_S_IXGRP = 0x0008; // execute
    public static final int EXT2_S_IRWXO = 0x0007; // others access right mask
    public static final int EXT2_S_IROTH = 0x0004; // read
    public static final int EXT2_S_IWOTH = 0x0002; // write
    public static final int EXT2_S_IXOTH = 0x0001; // execute

    // revision level values (stored in the superblock)
    public static final int EXT2_GOOD_OLD_REV = 0;
    public static final int EXT2_DYNAMIC_REV = 1;

    public static final int EXT2_PREALLOC_BLOCK = 7;

    // behaviour control flags in the inode
    public static final long EXT2_INDEX_FL = 0x00010000; // hash indexed directory

    // Filesystem state constants
    public static final int EXT2_VALID_FS = 0x0001; // cleanly unmounted
    public static final int EXT2_ERROR_FS = 0x0002;

    // what to do when errors are detected
    public static final int EXT2_ERRORS_CONTINUE = 0x0001;
    public static final int EXT2_ERRORS_RO = 0x0002;
    public static final int EXT2_ERRORS_PANIC = 0x0003;
    public static final int EXT2_ERRORS_DEFAULT = EXT2_ERRORS_CONTINUE;

    // S_FEATURE_RO_COMPAT constants
    public static final long EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER = 0x0001;
    public static final long EXT2_FEATURE_RO_COMPAT_LARGE_FILE = 0x0002;
    public static final long EXT2_FEATURE_RO_COMPAT_BTREE_DIR = 0x0004;

    // S_FEATURE_INCOMPAT constants
    public static final long EXT2_FEATURE_INCOMPAT_COMPRESSION = 0x0001;
    public static final long EXT2_FEATURE_INCOMPAT_FILETYPE = 0x0002;
    public static final long EXT3_FEATURE_INCOMPAT_RECOVER = 0x0004;
    public static final long EXT3_FEATURE_INCOMPAT_JOURNAL_DEV = 0x0008;
    public static final long EXT2_FEATURE_INCOMPAT_META_BG = 0x0010;

    // constants specific to this (JNode) implementation
    /**
     * When searching for free blocks, block groups that have at least
     * EXT2_BLOCK_THRESHOLD_RATIO/100 * BlocksPerGroup free blocks are
     * considered first - if this constant is too high, it will lead to higher
     * disk fragmentation - if it is too low, then files might be scattered
     * among multiple block groups on very full partitions (higher file
     * fragmentation) intervall: [0; 100)
     */
    public static final int EXT2_BLOCK_THRESHOLD_PERCENT = 5;

}
