package org.jnode.fs.ext2;

/**
 * @author Andras Nagy
 * 
 */
public class GroupDescriptor {
	public static final int GROUPDESCRIPTOR_LENGTH = 32;
	
	private byte data[];
	
	public GroupDescriptor(byte src[]) {
		data = new byte[src.length];
		System.arraycopy(src, 0, data, 0, src.length);
	}
	
	public int size() {
		return GROUPDESCRIPTOR_LENGTH;	
	}
	
	public long getBlockBitmap() {
		return Ext2Utils.get32(data, 0);
	}
			
	public long getInodeBitmap() {
		return Ext2Utils.get32(data, 4);
	}

	public long getInodeTable() {
		return Ext2Utils.get32(data, 8);
	}

	public int getFreeBlocksCount() {
		return Ext2Utils.get16(data, 12);
	}
	public int getFreeInodesCount() {
		return Ext2Utils.get16(data, 14);
	}
	public int getUsedDirsCount() {
		return Ext2Utils.get16(data, 16);
	}
}