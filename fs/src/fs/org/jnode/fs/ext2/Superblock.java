package org.jnode.fs.ext2;

import org.jnode.fs.FileSystemException;

/**
 * Ext2fs superblock
 * 
 * XXX implementation uncomplete
 * 
 * @author Andras Nagy
 */
public class Superblock {
	public static final int SUPERBLOCK_LENGTH = 1024;
	
	//revision level values
	public static final int EXT2_GOOD_OLD_REV = 0;
	public static final int EXT2_DYNAMIC_REV  = 1;
	
	private byte data[];
	
	public Superblock(byte src[]) throws FileSystemException {
		data = new byte[src.length];
		System.arraycopy(src, 0, data, 0, src.length);
			
		//check the magic number
		if(getMagic() != 0xEF53)
			throw new FileSystemException("Not ext2 superblock ("+getMagic()+": bad magic)");
	}
	
	public long getINodesCount() {
		return Ext2Utils.get32(data, 0);
	}
	
	public long getBlocksCount() {
		return Ext2Utils.get32(data, 4);
	}
	
	public long getRBlocksCount() {
		return Ext2Utils.get32(data, 8);
	}

	public long getFreeBlocksCount() {
		return Ext2Utils.get32(data, 12);
	}

	public long getFreeInodesCount() {
		return Ext2Utils.get32(data, 16);
	}

	public long getFirstDataBlock() {
		return Ext2Utils.get32(data, 20);
	}

	private long getLogBlockSize() {
		return Ext2Utils.get32(data, 24);
	}
	
	public int getBlockSize() {
		return 1024 << getLogBlockSize();
	}

	private long getLogFragSize() {
		return Ext2Utils.get32(data, 28);
	}
	
	public int getFragSize() {
		if(getLogFragSize()>0)
			return 1024 << getLogFragSize();
		else
			return 1024 >> -getLogFragSize();
	}

	public long getBlocksPerGroup() {
		return Ext2Utils.get32(data, 32);
	}

	public long getFragsPerGroup() {
		return Ext2Utils.get32(data, 36);
	}

	public long getINodesPerGroup() {
		return Ext2Utils.get32(data, 40);
	}

	public long getMTime() {
		return Ext2Utils.get32(data, 44);
	}

	public long getWTime() {
		return Ext2Utils.get32(data, 48);
	}
	
	public int getMntCount() {
		return Ext2Utils.get16(data, 52);
	}
	
	public int getMaxMntCount() {
		return Ext2Utils.get16(data, 54);
	}

	public int getMagic() {
		return Ext2Utils.get16(data, 56);
	}
		
	public int getState() {
		return Ext2Utils.get16(data, 58);
	}

	public int getErrors() {
		return Ext2Utils.get16(data, 60);
	}

	public int getMinorRevLevel() {
		return Ext2Utils.get16(data, 62);
	}
	
	public long getLastCheck() {
		return Ext2Utils.get32(data, 64);
	}
	
	public long getCheckInterval() {
		return Ext2Utils.get32(data, 68);
	}
	
	public long getCreatorOS() {
		return Ext2Utils.get32(data, 72);
	}
	
	public long getRevLevel() {
		return Ext2Utils.get32(data, 76);
	}
	
	public int getDefResuid() {
		return Ext2Utils.get16(data, 80);
	}
	
	public int getDefResgid() {
		return Ext2Utils.get16(data, 82);
	}

	public long getFirstInode() {
		if(getRevLevel()==EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 84);
		else 
			return 11;
	}
			
	public long getINodeSize() {
		if(getRevLevel()==EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 88);
		else 
			return 128;
	}

	//XXX what to return for old versions?
	public long getBlockGroupNr() {
		if(getRevLevel()==EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 90);
		else 
			return 0;
	}

	//XXX other values come...

}
