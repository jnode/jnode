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
		
	//some constants for the fs creation
	private static final long    BYTES_PER_INODE = 4096;	//one inode for 4KBs of data
	private static final double  RESERVED_BLOCKS_RATIO = 0.05;	//5% reserved for the superuser
	private static final boolean CREATE_WITH_SPARSE_SUPER = true;	//create the new fs with the sparse_super option
	private static final int 	 MAX_MOUNT_COUNT = 256;		//number of times to mount before check (check not yet implemented)
	private static final int 	 CHECK_INTERVAL = 365*24*60*60;	//check every year (check not yet implemented)
	private static final long 	 JNODE = 42;				//whatever
	
	private byte data[];
	private boolean dirty;
	private Ext2FileSystem fs;
	private final Logger log = Logger.getLogger(getClass());

	public Superblock() {		
		data = new byte[SUPERBLOCK_LENGTH];
		log.setLevel(Level.DEBUG);		
	}
	
	public void read(byte src[], Ext2FileSystem fs) throws FileSystemException {
		System.arraycopy(src, 0, data, 0, SUPERBLOCK_LENGTH);
		
		this.fs = fs;
			
		//check the magic :)
		if(getMagic() != 0xEF53)
			throw new FileSystemException("Not ext2 superblock ("+getMagic()+": bad magic)");
			
		setDirty(false);
	}
	
	private long divCeil(long a, long b) {
		return (long)Math.ceil((double)a/(double)b);
	}
	
	public void create(int blockSize, Ext2FileSystem fs) throws IOException {
		this.fs = fs;
		setRevLevel(Ext2Constants.EXT2_DYNAMIC_REV);
		setMinorRevLevel(0);
		setMagic(0xEF53);
		setCreatorOS(JNODE);
		
		//the number of inodes has to be <= than the number of blocks
		long bytesPerInode = BYTES_PER_INODE>=blockSize ? BYTES_PER_INODE : blockSize;
		long size = fs.getApi().getLength();
		long blocks = size / blockSize;
		long inodes = size / bytesPerInode;
		setINodesCount(inodes);
		setBlocksCount(blocks);
		setRBlocksCount( (long)(RESERVED_BLOCKS_RATIO * blocks) );
		setDefResgid(0);
		setDefResuid(0);
		setBlockSize(blockSize);		//actually sets the S_LOG_BLOCK_SIZE
		setFragSize(blockSize);			//set S_LOG_FRAG_SIZE
		setFirstDataBlock(blockSize==1024 ? 1 : 0);

		//a block bitmap is 1 block long, so blockSize*8 blocks can be indexed by a bitmap 
		//and thus be in a group
		long blocksPerGroup = blockSize << 3;
		setBlocksPerGroup(blocksPerGroup);
		setFragsPerGroup(blocksPerGroup);
		long groupCount = divCeil(blocks, blocksPerGroup);

		long inodesPerGroup = divCeil(inodes, groupCount);
		setINodesPerGroup(inodesPerGroup);	
		
		//calculate the number of blocks reserved for metadata
		//first, set the sparse_super option (it affects this value)
		if(CREATE_WITH_SPARSE_SUPER)
			setFeatureROCompat(getFeatureROCompat() | Ext2Constants.EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER);
		long sbSize  = 1;		//superblock is 1 block fixed	
		long gdtSize = divCeil(groupCount*GroupDescriptor.GROUPDESCRIPTOR_LENGTH, blockSize); 
		long bbSize  = 1;		//block bitmap is 1 block fixed
		long ibSize	 = 1;		//inode bitmap is 1 block fixed
		long inodeTableSize = divCeil( inodesPerGroup*INode.INODE_LENGTH, blockSize);
		int groupsWithMetadata = 0;
		for(int i=0; i<groupCount; i++)
			if(fs.groupHasDescriptors(i))
				groupsWithMetadata++;
		long metadataSize = (bbSize + ibSize + inodeTableSize) * groupCount + 
							(sbSize + gdtSize) * groupsWithMetadata;
		setFreeBlocksCount(blocks - metadataSize);
		setFirstInode(11);
		setFreeInodesCount(inodes - getFirstInode() + 1);
		
		setMTime(0);
		setWTime(0);
		setLastCheck(0);
		setCheckInterval(CHECK_INTERVAL);
		setMntCount(0);
		setMaxMntCount(MAX_MOUNT_COUNT);
		
		setState(Ext2Constants.EXT2_VALID_FS);
		setErrors(Ext2Constants.EXT2_ERRORS_DEFAULT);
		
		setINodeSize(INode.INODE_LENGTH);
		
		setBlockGroupNr(0);
		
		//set the options SPARSE_SUPER and FILETYPE
		setFeatureCompat(0);
		setFeatureROCompat(	Ext2Constants.EXT2_FEATURE_RO_COMPAT_SPARSE_SUPER );
		setFeatureIncompat(	Ext2Constants.EXT2_FEATURE_INCOMPAT_FILETYPE );
		
		byte[] uuid = new byte[16];
		for(int i=0; i<uuid.length; i++)
			uuid[i]=(byte)(Math.random()*255);
		setUUID(uuid);
		
		setPreallocBlocks(8);
		setPreallocDirBlocks(0);

		log.debug("Superblock.create(): getBlockSize(): "+getBlockSize());
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
				setBlockGroupNr(i);
				//update the old contents with the new superblock
				System.arraycopy(data, 0, oldData, 0, SUPERBLOCK_LENGTH);
				fs.writeBlock(blockNr, oldData, true);
			}
			
			setBlockGroupNr(0);
			setDirty(false);
		}
		
	}
	
	public long getINodesCount() {
		return Ext2Utils.get32(data, 0);
	}
	public void setINodesCount(long count) {
		Ext2Utils.set32(data, 0, count);
		setDirty(true);
	}
	
	public long getBlocksCount() {
		return Ext2Utils.get32(data, 4);
	}
	public void setBlocksCount(long count) {
		Ext2Utils.set32(data, 4, count);
		setDirty(true);
	}
	
	public long getRBlocksCount() {
		return Ext2Utils.get32(data, 8);
	}
	public void setRBlocksCount(long count) {
		Ext2Utils.set32(data, 8, count);
		setDirty(true);
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
	public void setFirstDataBlock(long i) {
		Ext2Utils.set32(data, 20, i);
		setDirty(true);
	}

	private long getLogBlockSize() {
		return Ext2Utils.get32(data, 24);
	}
	private void setLogBlockSize(long i) {
		Ext2Utils.set32(data, 24, i);
		setDirty(true);
	}
	
	public int getBlockSize() {
		return 1024 << getLogBlockSize();
	}
	public void setBlockSize(long size) {
		//setLogBlockSize( (long)(Math.log(size)/Math.log(2) - 10) );
		//Math.log() is buggy
		if(size==1024) setLogBlockSize(0);
		if(size==2048) setLogBlockSize(1);
		if(size==4096) setLogFragSize(2);
		if(size==8192) setLogFragSize(3);
		setDirty(true);
	}

	private long getLogFragSize() {
		return Ext2Utils.get32(data, 28);
	}
	private void setLogFragSize(long i) {
		Ext2Utils.set32(data, 28, i);
		setDirty(true);
	}
	
	public int getFragSize() {
		if(getLogFragSize()>0)
			return 1024 << getLogFragSize();
		else
			return 1024 >> -getLogFragSize();
	}
	public void setFragSize(long size) {
		//setLogFragSize( (long)(Math.log(size)/Math.log(2)) - 10 );
		//Math.log() is buggy
		if(size==64) setLogFragSize(-4);
		if(size==128) setLogFragSize(-3);
		if(size==256) setLogBlockSize(-2);
		if(size==512) setLogBlockSize(-1);		
		if(size==1024) setLogFragSize(0);
		if(size==2048) setLogFragSize(1);
		if(size==4096) setLogBlockSize(2);
		if(size==8192) setLogBlockSize(3);
		setDirty(true);
	}

	public long getBlocksPerGroup() {
		return Ext2Utils.get32(data, 32);
	}
	public void setBlocksPerGroup(long i) {
		Ext2Utils.set32(data, 32, i);
		setDirty(true);
	}

	public long getFragsPerGroup() {
		return Ext2Utils.get32(data, 36);
	}
	public void setFragsPerGroup(long i) {
		Ext2Utils.set32(data, 36, i);
		setDirty(true);
	}
	
	public long getINodesPerGroup() {
		return Ext2Utils.get32(data, 40);
	}
	public void setINodesPerGroup(long i) {
		Ext2Utils.set32(data, 40, i);
		setDirty(true);
	}

	public long getMTime() {
		return Ext2Utils.get32(data, 44);
	}
	public void setMTime(long time) {
		Ext2Utils.set32(data, 44, time);
		setDirty(true);
	}

	public long getWTime() {
		return Ext2Utils.get32(data, 48);
	}
	public void setWTime(long time) {
		Ext2Utils.set32(data, 48, time);
		setDirty(true);
	}
	
	public int getMntCount() {
		return Ext2Utils.get16(data, 52);
	}
	public void setMntCount(int i) {
		Ext2Utils.set16(data, 52, i);
		setDirty(true);
	}
	
	public int getMaxMntCount() {
		return Ext2Utils.get16(data, 54);
	}
	public void setMaxMntCount(int i) {
		Ext2Utils.set16(data, 54, i);
		setDirty(true);
	}

	public int getMagic() {
		return Ext2Utils.get16(data, 56);
	}
	public void setMagic(int i) {
		Ext2Utils.set16(data, 56, i);
		setDirty(true);
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
	public void setErrors(int i) {
		Ext2Utils.set16(data, 60, i);
		setDirty(true);
	}

	public int getMinorRevLevel() {
		return Ext2Utils.get16(data, 62);
	}
	public void setMinorRevLevel(int i) {
		Ext2Utils.set16(data, 62, i);
		setDirty(true);
	}	
	
	public long getLastCheck() {
		return Ext2Utils.get32(data, 64);
	}
	public void setLastCheck(long i) {
		Ext2Utils.set32(data, 64, i);
		setDirty(true);
	}
	
	public long getCheckInterval() {
		return Ext2Utils.get32(data, 68);
	}
	public void setCheckInterval(long i) {
		Ext2Utils.set32(data, 68, i);
		setDirty(true);
	}
	
	public long getCreatorOS() {
		return Ext2Utils.get32(data, 72);
	}
	public void setCreatorOS(long i) {
		Ext2Utils.set32(data, 72, i);
		setDirty(true);
	}
	
	public long getRevLevel() {
		return Ext2Utils.get32(data, 76);
	}
	public void setRevLevel(long i) {
		Ext2Utils.set32(data, 76, i);
		setDirty(true);
	}
	
	public int getDefResuid() {
		return Ext2Utils.get16(data, 80);
	}
	public void setDefResuid(int i) {
		Ext2Utils.set16(data, 80, i);
		setDirty(true);
	}
	
	public int getDefResgid() {
		return Ext2Utils.get16(data, 82);
	}
	public void setDefResgid(int i) {
		Ext2Utils.set16(data, 82, i);
		setDirty(true);
	}

	public long getFirstInode() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 84);
		else 
			return 11;
	}
	public void setFirstInode(long i) {
		Ext2Utils.set32(data, 84, i);
		setDirty(true);
	}
			
	public long getINodeSize() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get16(data, 88);
		else 
			return INode.INODE_LENGTH;
	}
	public void setINodeSize(int i) {
		Ext2Utils.set16(data, 88, i);
		setDirty(true);
	}

	//XXX what to return for old versions?
	public long getBlockGroupNr() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get16(data, 90);
		else 
			return 0;
	}
	public void setBlockGroupNr(int i) {
		Ext2Utils.set16(data, 90, i);
		setDirty(true);
	}
	
	public long getFeatureCompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 92);
		else 
			return 0;
	}
	public void setFeatureCompat(long i) {
		Ext2Utils.set32(data, 92, i);
		setDirty(true);
	}

	public long getFeatureIncompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 96);
		else 
			return 0;
	}
	public void setFeatureIncompat(long i) {
		Ext2Utils.set32(data, 96, i);
		setDirty(true);
	}
	
	public long getFeatureROCompat() {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			return Ext2Utils.get32(data, 100);
		else 
			return 0;
	}
	public void setFeatureROCompat(long i) {
		Ext2Utils.set32(data, 100, i);
		setDirty(true);
	}

	public byte[] getUUID() {
		byte[] result=new byte[16];	
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			System.arraycopy(data, 104, result, 0, 16);
		return result;
	}
	public void setUUID(byte[] uuid) {
		if(getRevLevel()==Ext2Constants.EXT2_DYNAMIC_REV)
			System.arraycopy(uuid, 0, data, 104, 16);		
		setDirty(true);
	}

	//	TODO: setters from this point on...

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
	public void setPreallocBlocks(int i) {
		Ext2Utils.set8(data, 204, i);
		setDirty(true);
	}

	public int getPreallocDirBlocks() {
		return Ext2Utils.get8(data, 205);
	}
	public void setPreallocDirBlocks(int i) {
		Ext2Utils.set8(data, 205, i);
		setDirty(true);
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
