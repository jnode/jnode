package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 */
public class INode {

	private static final Logger log = Logger.getLogger(Ext2FileSystemType.class);
	public static final int INODE_LENGTH = 128;

	public static final int EXT2_BAD_INO = 0x01; //bad blocks inode
	public static final int EXT2_ROOT_INO = 0x02; //root directory inode
	public static final int EXT2_ACL_IDX_INO = 0x03; //ACL index node
	public static final int EXT2_ACL_DATA_INO = 0x04; //ACL data inode
	public static final int EXT2_BOOT_LOADER_INO = 0x05; //boot loader inode
	public static final int EXT2_UNDEL_DIR_INO = 0x06; //undelete directory
	// inode

	//i_mode values
	public static final int EXT2_S_IFMT = 0xF000; //format mask
	public static final int EXT2_S_IFSOCK = 0xC000; //socket
	public static final int EXT2_S_IFLNK = 0xA000; //symbolic link
	public static final int EXT2_S_IFREG = 0x8000; //regular file
	public static final int EXT2_S_IFBLK = 0x6000; //block device
	public static final int EXT2_S_IFDIR = 0x4000; //directory
	public static final int EXT2_S_IFCHR = 0x2000; //character device
	public static final int EXT2_S_IFIFO = 0x1000; //fifo
	//access rights
	public static final int EXT2_S_ISUID = 0x0800; //SUID
	public static final int EXT2_S_ISGID = 0x0400; //SGID
	public static final int EXT2_S_ISVTX = 0x0200; //sticky bit
	public static final int EXT2_S_IRWXU = 0x01C0; //user access right mask
	public static final int EXT2_S_IRUSR = 0x0100; //read
	public static final int EXT2_S_IWUSR = 0x0080; //write
	public static final int EXT2_S_IXUSR = 0x0040; //execute
	public static final int EXT2_S_IRWXG = 0x0038; //group access right mask
	public static final int EXT2_S_IRGRP = 0x0020; //read
	public static final int EXT2_S_IWGRP = 0x0010; //write
	public static final int EXT2_S_IXGRP = 0x0008; //execute
	public static final int EXT2_S_IRWXO = 0x0007; //others access right mask
	public static final int EXT2_S_IROTH = 0x0004; //read
	public static final int EXT2_S_IWOTH = 0x0002; //write
	public static final int EXT2_S_IXOTH = 0x0001; //execute

	//the data constituting the inode itself
	private byte[] data;

	private Ext2FileSystem fs;

	public INode(Ext2FileSystem fs, byte[] data) {
		this.fs = fs;
		this.data = new byte[INODE_LENGTH];
		System.arraycopy(data, 0, this.data, 0, INODE_LENGTH);
	}

	public INode(Ext2FileSystem fs, int iNodeNr) {
		// empty
	}

	public Ext2FileSystem getExt2FileSystem() {
		return fs;
	}

	public int getIMode() {
		int iMode = Ext2Utils.get16(data, 0);
		log.debug("INode.getIMode(): " + Ext2Debugger.hexFormat(iMode));
		return iMode;
	}

	public int getIUid() {
		return Ext2Utils.get16(data, 2);
	}

	public long getISize() {
		return Ext2Utils.get32(data, 4);
	}

	public byte[] getDataBlock(long i) throws IOException {
		//get the direct blocks (0; 11)
		if (i < 12)
			return fs.getBlock(Ext2Utils.get32(data, 40 + (int)i * 4));

		//see the indirect blocks (12; indirectCount-1)
		int indirectCount = fs.getSuperblock().getBlockSize() << 2; //a block
		// index is 4
		// bytes long
		if (i < 12 + indirectCount) {
			long offset = i - 12;
			//the 12th index points to the indirect block
			byte[] indirectBlock = fs.getBlock(Ext2Utils.get32(data, 40 + 12 * 4));
			long blockIndex = Ext2Utils.get32(indirectBlock, (int)offset * 4);
			return fs.getBlock(blockIndex);
		}

		//see the double indirect blocks (indirectCount; doubleIndirectCount-1)
		int doubleIndirectCount = indirectCount * indirectCount;
		if (i < 12 + indirectCount + doubleIndirectCount) {
			//the 13th index points to the double indirect block
			byte[] doubleIndirectBlock = fs.getBlock(Ext2Utils.get32(data, 40 + 13 * 4));
			long offset = i - indirectCount - 12;
			long indirectBlockNr = offset / indirectCount;
			long indirectBlockOffset = offset % indirectCount;

			byte[] indirectBlock = fs.getBlock(Ext2Utils.get32(doubleIndirectBlock, (int)indirectBlockNr));
			long blockIndex = Ext2Utils.get32(indirectBlock, (int)indirectBlockOffset * 4);

			return fs.getBlock(blockIndex);
		}

		//see the triple indirect blocks (doubleIndirectCount;
		// tripleIndirectCount-1)
		int tripleIndirectCount = indirectCount * indirectCount * indirectCount;
		if (i < 12 + indirectCount + doubleIndirectCount + tripleIndirectCount) {
			//the 14th index points to the triple indirect block
			byte[] tripleIndirectBlock = fs.getBlock(Ext2Utils.get32(data, 40 + 14 * 4));
			long offset = i - doubleIndirectCount - indirectCount - 12;
			long doubleIndirectBlockNr = offset / (indirectCount * indirectCount);
			long doubleIndirectBlockOffset = offset % (indirectCount * indirectCount);

			byte[] doubleIndirectBlock = fs.getBlock(Ext2Utils.get32(tripleIndirectBlock, (int)doubleIndirectBlockNr));
			long indirectBlockIndex = Ext2Utils.get32(doubleIndirectBlock, (int)doubleIndirectBlockOffset * 4);
			long indirectBlockOffset = offset % indirectCount;

			byte[] indirectBlock = fs.getBlock(Ext2Utils.get32(doubleIndirectBlock, (int)indirectBlockIndex * 4));
			long blockIndex = Ext2Utils.get32(indirectBlock, (int)indirectBlockOffset * 4);

			return fs.getBlock(blockIndex);
		} else {
			throw new IOException(
				"file too big: more than "
					+ 12
					+ indirectCount
					+ doubleIndirectCount
					+ tripleIndirectCount
					+ " blocks");
		}

	}

}
