 /*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 * 
 */
public class GroupDescriptor {
	public static final int GROUPDESCRIPTOR_LENGTH = 32;
	
	private byte data[];
	private Ext2FileSystem fs;
	private int groupNr;
	private boolean dirty;
	private final Logger log = Logger.getLogger(getClass());

	public GroupDescriptor() {
		data = new byte[GROUPDESCRIPTOR_LENGTH];
		log.setLevel(Level.DEBUG);		
	}
	
	public void read(int groupNr, Ext2FileSystem fs) throws IOException{
		//read the group descriptors from the main copy in block group 0
		//byte[] blockData = fs.getBlock( fs.getSuperblock().getFirstDataBlock() + 1);
		long baseBlock  = fs.getSuperblock().getFirstDataBlock()+1;
		long blockOffset=(groupNr*GROUPDESCRIPTOR_LENGTH)/fs.getBlockSize();
		long offset 	=(groupNr*GROUPDESCRIPTOR_LENGTH)%fs.getBlockSize();
		byte[] blockData= fs.getBlock(baseBlock + blockOffset);
		
		//data = new byte[GROUPDESCRIPTOR_LENGTH];
		System.arraycopy(blockData, (int)offset, data, 0, GROUPDESCRIPTOR_LENGTH);
		this.groupNr = groupNr;
		this.fs = fs;
		setDirty(false);
	}
	
	private long divCeil(long a, long b) {
		return (long)Math.ceil((double)a/(double)b);
	}
	
	public void create(int groupNr, Ext2FileSystem fs) {
		this.fs = fs;
		this.groupNr = groupNr;
		
		long desc;		//the length of the superblock and group descriptor copies in the block group
		if(!fs.groupHasDescriptors(groupNr))
			desc = 0; 
		else
			desc =	1 + 				/* superblock */
					divCeil(fs.getGroupCount()*GroupDescriptor.GROUPDESCRIPTOR_LENGTH, fs.getBlockSize());	/* GDT */

		setBlockBitmap( fs.getSuperblock().getFirstDataBlock() +  
						groupNr*fs.getSuperblock().getBlocksPerGroup() +
						desc );

		setInodeBitmap( getBlockBitmap() + 1 );
		setInodeTable( getBlockBitmap() + 2);
		
		long inodeTableSize = divCeil( fs.getSuperblock().getINodesPerGroup()*INode.INODE_LENGTH, fs.getBlockSize());	
		long blockCount;
		if(groupNr == fs.getGroupCount()-1)
			blockCount =  fs.getSuperblock().getBlocksCount() 
						- fs.getSuperblock().getBlocksPerGroup()*(fs.getGroupCount()-1)
						- fs.getSuperblock().getFirstDataBlock();
		else
			blockCount = fs.getSuperblock().getBlocksPerGroup();
		
		setFreeBlocksCount( (int)(blockCount 
				- desc				/* superblock copy, GDT copies */ 
				- 2					/* block and inode bitmaps */
				- inodeTableSize));	/* inode table */
		
		if(groupNr==0)
			setFreeInodesCount( (int)(fs.getSuperblock().getINodesPerGroup() - fs.getSuperblock().getFirstInode() + 1) );
		else
			setFreeInodesCount( (int)(fs.getSuperblock().getINodesPerGroup()) );
				
		setUsedDirsCount(0);
	}
	
	/**
	 * GroupDescriptors are duplicated in some (or all) block groups: if a GroupDescriptor changes,
	 * all copies have to be changed.
	 */
	protected void updateGroupDescriptors() throws IOException{
		//all the copies of the group descriptors have to be modified in sync
		if(isDirty()) {
			log.debug("Updating groupdescriptor copies");
			synchronized(fs.getGroupDescriptorLock()) {
				for(int i=0; i<fs.getGroupCount(); i++) {
					//check if there is a group descriptor table copy in the block group
					if(!fs.groupHasDescriptors(i))
						continue;
					
					long block  = 	fs.getSuperblock().getFirstDataBlock() + 1 +
									fs.getSuperblock().getBlocksPerGroup() * i;	//<- for the ith block group
					long pos = groupNr*GROUPDESCRIPTOR_LENGTH;
					block      += pos / fs.getBlockSize();			
					long offset = pos % fs.getBlockSize();
					byte[] blockData = fs.getBlock( block );
					//update the block with the new group descriptor
					System.arraycopy(data, 0, blockData, (int)offset, GROUPDESCRIPTOR_LENGTH);
					fs.writeBlock( block, blockData, true); 							
				}
			}
			setDirty(false);
		}
	}

	
	public int size() {
		return GROUPDESCRIPTOR_LENGTH;	
	}
	
	public long getBlockBitmap() {
		return Ext2Utils.get32(data, 0);
	}
	public void setBlockBitmap(long l) {
		Ext2Utils.set32(data, 0, l);
		setDirty(true);
	}
			
	public long getInodeBitmap() {
		return Ext2Utils.get32(data, 4);
	}
	public void setInodeBitmap(long l) {
		Ext2Utils.set32(data, 4, l);
		setDirty(true);
	}

	public long getInodeTable() {
		return Ext2Utils.get32(data, 8);
	}
	public void setInodeTable(long l) {
		Ext2Utils.set32(data, 8, l);
		setDirty(true);
	}

	public int getFreeBlocksCount() {
		return Ext2Utils.get16(data, 12);
	}
	public void setFreeBlocksCount(int count) {
		Ext2Utils.set16(data, 12, count);
		setDirty(true);
	}
	
	public int getFreeInodesCount() {
		return Ext2Utils.get16(data, 14);		
	}
	public void setFreeInodesCount(int count) {
		Ext2Utils.set16(data, 14, count);
		setDirty(true);
	}

	public int getUsedDirsCount() {
		return Ext2Utils.get16(data, 16);
	}
	public void setUsedDirsCount(int count) {
		Ext2Utils.set16(data, 16, count);
		setDirty(true);
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