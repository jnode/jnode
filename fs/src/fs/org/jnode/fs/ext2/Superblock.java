/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;

/**
 * Ext2fs superblock
 * 
 * @author Andras Nagy
 */
public class Superblock {
	public static final int SUPERBLOCK_LENGTH = 1024;
		
	private byte data[];
	private boolean dirty;
	private Ext2FileSystem fs;
	private final Logger log = Logger.getLogger(getClass());

	public Superblock(byte src[], Ext2FileSystem fs) throws FileSystemException {
		data = new byte[src.length];
		System.arraycopy(src, 0, data, 0, src.length);
		
		this.fs = fs;
		log.setLevel(Level.DEBUG);
			
		//check the magic :)
		if(getMagic() != 0xEF53)
			throw new FileSystemException("Not ext2 superblock ("+getMagic()+": bad magic)");
			
		setDirty(false);
	}
	
	/**
	 * Update the superblock copies on the disk
	 */
	public void update() throws IOException {
		if(isDirty()) {
			log.debug("Updating superblock copies");
			byte[] oldData;
			
			//update the main copy
			if(getFirstDataBlock()==0) {
				oldData=fs.getBlock(0);
				//the block size is an integer multiply of 1024, and if getFirstDataBlock==0, it's
				//at least 2048 bytes
				System.arraycopy(data, 0, oldData, 1024, SUPERBLOCK_LENGTH);
			} else {
				oldData=fs.getBlock(getFirstDataBlock());
				System.arraycopy(data, 0, oldData, 0, SUPERBLOCK_LENGTH);				
			}
			fs.writeBlock(getFirstDataBlock(), oldData, true);
			
			//update the other copies
			for(int i=1; i<fs.getGroupCount(); i++) {
				//check if there is a superblock copy in the block group
				if(!fs.groupHasDescriptors(i))
					continue;

				long blockNr=getFirstDataBlock() + i*getBlocksPerGroup();
				oldData = fs.getBlock(blockNr);
				//update the old contents with the new superblock
				System.arraycopy(data, 0, oldData, 0, SUPERBLOCK_LENGTH);
				fs.writeBlock(blockNr, oldData, true);
			}
			
			setDirty(false);
		}
		
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
	public void setFreeBlocksCount(long count) {
		Ext2Utils.set32(data, 12, count);
		setDirty(true);
	}

	public long getFreeInodesCount() {
		return Ext2Utils.get32(data, 16);
	}
	public void setFreeInodesCount(long count) {
		Ext2Utils.set32(data, 16, count);
		setDirty(true);
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
	public void setState(int state) {
		Ext2Utils.set16(data, 58, state);
		setDirty(true);
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
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 84);
		else 
			return 11;
	}
			
	public long getINodeSize() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get16(data, 88);
		else 
			return 128;
	}

	//XXX what to return for old versions?
	public long getBlockGroupNr() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get16(data, 90);
		else 
			return 0;
	}
	
	public long getFeatureCompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 92);
		else 
			return 0;
	}

	public long getFeatureIncompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 96);
		else 
			return 0;
	}
	
	public long getFeatureROCompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 100);
		else 
			return 0;
	}

	public byte[] getUUID() {
		byte[] result=new byte[16];	
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			System.arraycopy(data, 104, result, 0, 16);
		return result;
	}
	
	public String getVolumeName() {
		StringBuffer result=new StringBuffer();
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			for(int i=0; i<16; i++) {
				char c=(char)data[120+i];
				if(c!=0)
					result.append(c);
				else
					break;
			}	
		return result.toString();
	}
	
	public String getLastMounted() {
		StringBuffer result=new StringBuffer();
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			for(int i=0; i<64; i++) {
				char c=(char)data[136+i];
				if(c!=0)
					result.append(c);
				else
					break;
			}	
		return result.toString();
	}

	//not sure this is the correct byte-order for this field
	public long getAlgoBitmap() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 200);
		else 
			return 11;
	}
	
	public int getPreallocBlocks() {
		return Ext2Utils.get8(data, 204);
	}

	public int getPreallocDirBlocks() {
		return Ext2Utils.get8(data, 205);
	}

	public byte[] getJournalUUID() {
		byte[] result=new byte[16];	
		System.arraycopy(data, 208, result, 0, 16);
		return result;
	}
	
	public long getJournalINum() {
		return Ext2Utils.get32(data, 224);
	}

	public long getJournalDev() {
		return Ext2Utils.get32(data, 228);
	}

	public long getLastOrphan() {
		return Ext2Utils.get8(data, 232);
	}
	/**
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @param b
	 */
	public void setDirty(boolean b) {
		dirty = b;
	}

}
