/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.AbstractFileSystem;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.ext2.cache.Block;
import org.jnode.fs.ext2.cache.BlockCache;
import org.jnode.fs.ext2.cache.INodeCache;

/**
 * @author Andras Nagy
 * 
 */
public class Ext2FileSystem extends AbstractFileSystem {
	private Superblock superblock;
	private GroupDescriptor groupDescriptors[];
	private int groupCount;
	private BlockCache blockCache;
	private INodeCache inodeCache;
	private final Logger log = Logger.getLogger(getClass());
	
	private Object groupDescriptorLock;
	private Object superblockLock;
	
	//private final boolean DEBUG=true;
	
	//TODO: SYNC_WRITE should be made a parameter
	/** if true, writeBlock() does not return until the block is written to disk */
	private boolean SYNC_WRITE = true;

	/**
	 * Constructor for Ext2FileSystem in specified readOnly mode
	 * @throws FileSystemException 
	 */
	public Ext2FileSystem(Device device, boolean readOnly)  throws FileSystemException {
		super(device, readOnly);
			
		byte data[];
		
		blockCache = new BlockCache(50,(float)0.75);
		inodeCache = new INodeCache(50,(float)0.75);
		
		groupDescriptorLock = new Object();
		superblockLock = new Object();

		try {
			data = new byte[Superblock.SUPERBLOCK_LENGTH];

			//skip the first 1024 bytes (bootsector) and read the superblock 
			//TODO: the superblock should read itself
			getApi().read(1024, data, 0, Superblock.SUPERBLOCK_LENGTH);
			superblock = new Superblock(data, this);
			
			//read the group descriptors
			groupCount = (int)Math.ceil((double)superblock.getBlocksCount() / (double)superblock.getBlocksPerGroup());
			groupDescriptors = new GroupDescriptor[groupCount];
			
			//OLD VERSION
			for(int i=0; i<groupCount; i++) {
			   data=getBlock(superblock.getFirstDataBlock()+1);    
			   groupDescriptors[i] = new GroupDescriptor(data, this, i);
			} 
			//OLD VERSION
			       
			/*
			for(int i=0; i<groupCount; i++) {
				groupDescriptors[i]=new GroupDescriptor(i, this);
			}
			*/	
		} catch (FileSystemException e) {
			throw e;
		} catch (Exception e) {
			throw new FileSystemException(e);
		}		
		
		log.info(  "Ext2fs filesystem constructed sucessfully");
		log.debug( "	superblock:	#blocks:		"+superblock.getBlocksCount()+"\n"+
					"				#blocks/group:	"+superblock.getBlocksPerGroup()+"\n"+
					"				#block groups:	"+groupCount+"\n"+
					"				block size:		"+superblock.getBlockSize()+"\n"+
					"				#inodes:		"+superblock.getINodesCount()+"\n"+
					"				#inodes/group:	"+superblock.getINodesPerGroup());
	}

	/**
	 * Flush all changed structures to the device.
	 * @throws IOException
	 */
	public void flush() throws IOException {
		log.info("Flushing the contents of the filesystem");
		//update the inodes
		synchronized(inodeCache) {
			try{
				log.debug("inodecache size: "+inodeCache.size());
				Iterator iterator = inodeCache.values().iterator();
				while(iterator.hasNext()) {
					INode iNode = (INode)iterator.next();
					iNode.flush();
					log.debug("flush(): inodeCache.values().iterator().hasNext() "+iterator.hasNext());
				}
			}catch(FileSystemException fse) {
				throw new IOException(fse);
			}
		}
		
		//update the group descriptors and the superblock copies
		updateFS();
		
		//flush the blocks
		synchronized(blockCache) {
			Iterator iterator = blockCache.values().iterator();
			while(iterator.hasNext()) {
				Block block = (Block)iterator.next();
				block.flush();
			}		
		}

		log.info("Filesystem flushed");
	}
	
	protected void updateFS() throws IOException {
		//updating one group descriptor updates all its copies
		for(int i=0; i<groupCount; i++)
			groupDescriptors[i].updateGroupDescriptors();
		superblock.update();
	}

	/**
	 * @see org.jnode.fs.FileSystem#getRootEntry()
	 */
	public FSEntry getRootEntry() throws IOException {
		try{
			if(!isClosed()) {
				return new Ext2Entry( getINode(Ext2Constants.EXT2_ROOT_INO), 
									  "/", Ext2Constants.EXT2_FT_DIR, this );
			}
		}catch(FileSystemException e) {
			throw new IOException(e);
		}
		return null;
	}
	
	/**
	 * Return the block size of the file system
	 */
	public long getBlockSize() {
		return superblock.getBlockSize();	
	}

	/** 
	 * Read a data block and put it in the cache if it is not yet cached,
	 * otherwise get it from the cache.
	 * 
	 * Synchronized access to the blockCache is important as the bitmap 
	 * operations are synchronized to the blocks (actually, to Block.getData()), 
	 * so at any point in time it has to be sure that no two copies of the same 
	 * block are stored in the cache.
	 * 
	 * @return data block nr
	 */
	public byte[] getBlock(long nr) throws IOException{

		//log.debug("blockCache size: "+blockCache.size());
		
		int blockSize = superblock.getBlockSize();
		Block result;
		
		Integer key=new Integer((int)(nr));
		synchronized(blockCache) {
			//check if the block has already been retrieved
			if(blockCache.containsKey(key)) 
				result=(Block)blockCache.get(key);
			else{
				byte[] data = new byte[blockSize];
				//api.read( nr*blockSize, data, 0, blockSize );
				timedRead(nr, data);
				result=new Block(this, nr, data);
				blockCache.put(key, result);
			}
		}
			
		return result.getData();
	}
	
	/**
	 * Update the block in cache, or write the block to disk
	 * @param nr:	block number
	 * @param data:	block data
	 * @param forceWrite: if forceWrite is false, the block is only updated
	 * in the cache (if it was in the cache). If forceWrite is true, or the
	 * block is not in the cache, write it to disk.
	 * @throws IOException
	 */
	public void writeBlock(long nr, byte[] data, boolean forceWrite) throws IOException {
		if(isReadOnly())
			throw new ReadOnlyFileSystemException("Filesystem is mounted read-only!"); 
			
		Block block;
		
		Integer key=new Integer((int)nr);
		//check if the block is in the cache
		synchronized(blockCache) {
			if(blockCache.containsKey(key)) {
				block = (Block)blockCache.get(key);
				//update the data in the cache
				block.setData(data);
				if(forceWrite || SYNC_WRITE) {
					//write the block to disk
					//api.write(nr*blockSize, data, 0, blockSize);
					timedWrite(nr, data);
					block.setDirty(false);

					log.debug("writing block "+nr+" to disk");
				} else
					block.setDirty(true);
			} else {
				//If the block was not in the cache, I see no reason to put it
				//in the cache when it is written.
				//It is simply written to disk.
				//api.write(nr*blockSize, data, 0, blockSize);
				timedWrite(nr, data);
			}
		}
	}
	
	/**
	 * Helper class for timedWrite
	 * @author blind
	 */
	class TimeoutWatcher extends TimerTask {
			Thread mainThread;
			public TimeoutWatcher(Thread mainThread) {
				this.mainThread = mainThread;
			}
			public void run() {
				mainThread.interrupt();
			}
	}
	
	private static final long TIMEOUT = 100;
	/**
	 * timedWrite writes to disk and waits for timeout, if the operation does not finish
	 * in time, restart it.
	 *  DO NOT CALL THIS DIRECTLY! ONLY TO BE CALLED FROM writeBlock()!
	 * @param nr		the number of the block to write
	 * @param data		the data in the block
	 */
	private void timedWrite(long nr, byte[] data) throws IOException{
		boolean finished = false;
		Timer writeTimer;
		while(!finished) {
			finished = true;
			writeTimer = new Timer();
			writeTimer.schedule(new TimeoutWatcher(Thread.currentThread()), TIMEOUT);
			try{
				getApi().write(nr*getBlockSize(), data, 0, (int)getBlockSize());
				writeTimer.cancel();
			}catch(IOException ioe) {
				//IDEDiskDriver will throw an IOException with a cause of an InterruptedException
				//it the write is interrupted
				if(ioe.getCause() instanceof InterruptedException) {
					writeTimer.cancel();
					log.debug("IDE driver interrupted during write operation: probably timeout");
					finished = false;
				}
			}
		}
	}

	private void timedRead(long nr, byte[] data) throws IOException{
		boolean finished = false;
		Timer readTimer;
		while(!finished) {
			finished = true;
			readTimer = new Timer();
			readTimer.schedule(new TimeoutWatcher(Thread.currentThread()), TIMEOUT);
			try{
				getApi().read( nr*getBlockSize(), data, 0, (int)getBlockSize());
				readTimer.cancel();
			}catch(IOException ioe) {
				//IDEDiskDriver will throw an IOException with a cause of an InterruptedException
				//it the write is interrupted
				if(ioe.getCause() instanceof InterruptedException) {
					readTimer.cancel();
					log.debug("IDE driver interrupted during read operation: probably timeout");
					finished = false;
				}
			}
		}
	}
		
	public Superblock getSuperblock() {
		return superblock;
	}	

	/** 
	 * Return the inode numbered inodeNr (the first inode is #1)
	 */
	public INode getINode(int iNodeNr) throws IOException, FileSystemException{
		if((iNodeNr<1)||(iNodeNr>superblock.getINodesCount()))
			throw new FileSystemException("INode number ("+iNodeNr+") out of range (0-"+superblock.getINodesCount()+")");
			
		Integer key=new Integer(iNodeNr);
		
		//log.debug("iNodeCache size: "+inodeCache.size());
		
		synchronized(inodeCache) {
			//check if the inode is already in the cache
			if(inodeCache.containsKey(key)) 
				return (INode)inodeCache.get(key);
			else{
				int group = (int) ((iNodeNr - 1) / superblock.getINodesPerGroup());
				int index = (int) ((iNodeNr - 1) % superblock.getINodesPerGroup());
		
				//get the part of the inode table that contains the inode
				long iNodeTableBlock  = groupDescriptors[group].getInodeTable();	//the first block of the inode table
				INodeTable iNodeTable = new INodeTable(this, (int)iNodeTableBlock);
				INode result = new INode(this, 
										 new INodeDescriptor(iNodeTable, iNodeNr, group, index),
										 iNodeTable.getInodeData(index));		
				
				inodeCache.put(key, result);
				
				return result;
			}
		}
	}

	/**
	 * Checks whether block <code>blockNr</code> is free, and if it is, then allocates it
	 * with preallocation.
	 * @param blockNr
	 * @return
	 * @throws IOException
	 */
	public BlockReservation testAndSetBlock(long blockNr) throws IOException{

		if(blockNr<superblock.getFirstDataBlock() || blockNr>=superblock.getBlocksCount())
			return new BlockReservation(false, -1, -1);
		int group = translateToGroup(blockNr);
		int index = translateToIndex(blockNr);

		/* Return false if the block is not a data block but a filesystem
		 * metadata block, as the beginning of each block group is filesystem
		 * metadata:
		 * superblock copy (if present)
		 * block bitmap
		 * inode bitmap
		 * inode table
		 * Free blocks begin after the inode table.
		 */
		long iNodeTableBlock = 	groupDescriptors[group].getInodeTable();
		long firstNonMetadataBlock = iNodeTableBlock + INodeTable.getSizeInBlocks(this);
		 
		if(blockNr<firstNonMetadataBlock)
			return new BlockReservation(false, -1, -1);

		byte[] bitmap = getBlock(groupDescriptors[group].getBlockBitmap());
		
		//at any time, only one copy of the Block exists in the cache, so it is
		//safe to synchronize to the bitmapBlock object (it's part of Block)
		synchronized( bitmap ) {
			BlockReservation result = BlockBitmap.testAndSetBlock( bitmap, index );
			//update the block bitmap
			if(result.isSuccessful()) {
				writeBlock(groupDescriptors[group].getBlockBitmap(), bitmap, false);
				modifyFreeBlocksCount(group, -1-result.getPreallocCount());
				result.setBlock( result.getBlock()+superblock.getFirstDataBlock() );
			}
			return result;
		}
	}
	
	/**
	 * Create a new INode
	 * @param preferredBlockBroup: first try to allocate the inode in this block group
	 * @return
	 */
	protected INode createINode(int preferredBlockBroup, 
		int fileFormat, int accessRights,
		int uid, int gid) 
	throws FileSystemException, IOException {
		if(preferredBlockBroup >= superblock.getBlocksCount())
			throw new FileSystemException("Block group "+preferredBlockBroup+"does not exist");
			
		int groupNr = preferredBlockBroup;
		//first check the preferred block group, if it has any free inodes
		INodeReservation res = findFreeINode( groupNr );
		
		//if no free inode has been found in the preferred block group, then try the others
		if(!res.isSuccessful()) {
			for(groupNr=0; groupNr<superblock.getBlockGroupNr(); groupNr++) {
				res = findFreeINode( groupNr );
				if(res.isSuccessful()){
					break;
				}		
			}
		}
		
		if(!res.isSuccessful())
			throw new FileSystemException("No free inodes found!");	
		
		//a free inode has been found: create the inode and write it into the inode table			
		long iNodeTableBlock  = groupDescriptors[preferredBlockBroup].getInodeTable();	//the first block of the inode table
		INodeTable iNodeTable = new INodeTable(this, (int)iNodeTableBlock);
		//byte[] iNodeData = new byte[INode.INODE_LENGTH];
		int iNodeNr = res.getINodeNr((int)superblock.getINodesPerGroup());
		INode iNode = new INode(this, 
								new INodeDescriptor(iNodeTable, iNodeNr, groupNr, res.getIndex()),
								fileFormat, accessRights,
								uid, gid);		
		//trigger a write to disk
		iNode.update();

		log.debug("** NEW INODE ALLOCATED: inode number: "+iNode.getINodeNr());			
		
		//put the inode into the cache
		synchronized(inodeCache) {
			Integer key = new Integer(iNodeNr);
			if(inodeCache.containsKey(key)) 
				throw new FileSystemException("Newly allocated inode is already in the inode cache!?");
			else
				inodeCache.put(key, iNode);
		}
		
		return iNode;		
	}
	
	/**
	 * Find a free INode in the inode bitmap and allocate it
	 * @param blockGroup
	 * @return
	 * @throws IOException
	 */
	protected INodeReservation findFreeINode(int blockGroup) throws IOException{
		GroupDescriptor gdesc = groupDescriptors[blockGroup];
		if(gdesc.getFreeInodesCount()> 0) {
			byte[] bitmap = getBlock( gdesc.getInodeBitmap() );
			synchronized(bitmap) {
				INodeReservation result = INodeBitmap.findFreeINode(bitmap);
				
				if(result.isSuccessful()){
					//update the inode bitmap
					writeBlock( gdesc.getInodeBitmap(), bitmap, true);
					modifyFreeInodesCount(blockGroup, -1);
					
					result.setGroup(blockGroup);
															
					return result;
				}
			}
		}
		return new INodeReservation(false, -1);
	}

	protected int translateToGroup(long i) {
		return (int)((i-superblock.getFirstDataBlock()) / superblock.getBlocksPerGroup());
	}
	
	protected int translateToIndex(long i) {
		return (int)((i-superblock.getFirstDataBlock()) % superblock.getBlocksPerGroup());
	}
	
	/**
	 * Modify the number of free blocks in the block group
	 * @param group
	 * @param diff can be positive or negative
	 */
	protected void modifyFreeBlocksCount(int group, int diff) throws IOException {
		GroupDescriptor gdesc = groupDescriptors[group];
		gdesc.setFreeBlocksCount( gdesc.getFreeBlocksCount()+diff );
		
		superblock.setFreeBlocksCount( superblock.getFreeBlocksCount()+diff );
	}
	
	/**
	 * Modify the number of free inodes in the block group
	 * @param group
	 * @param diff can be positive or negative
	 */
	protected void modifyFreeInodesCount(int group, int diff) throws IOException {
		GroupDescriptor gdesc = groupDescriptors[group];
		gdesc.setFreeInodesCount( gdesc.getFreeInodesCount()+diff );
		
		superblock.setFreeInodesCount( superblock.getFreeInodesCount()+diff );
	}
	
	/**
	 * Free up a block in the block bitmap.
	 * @param blockNr
	 * @throws FileSystemException
	 * @throws IOException
	 */
	public void freeBlock(long blockNr) throws FileSystemException, IOException{
		if(blockNr<0 || blockNr>=superblock.getBlocksCount())
			throw new FileSystemException("Attempt to free nonexisting block ("+blockNr+")");
			
		int group = translateToGroup(blockNr);
		int index = translateToIndex(blockNr);
		GroupDescriptor gdesc = groupDescriptors[group];		

		/* Throw an exception if an attempt is made to free up a filesystem metadata
		 * block (the beginning of each block group is filesystem metadata):
		 * superblock copy (if present)
		 * block bitmap
		 * inode bitmap
		 * inode table
		 * Free blocks begin after the inode table.
		 */
		long iNodeTableBlock = 	groupDescriptors[group].getInodeTable();
		long firstNonMetadataBlock = iNodeTableBlock + INodeTable.getSizeInBlocks(this);
		 
		if(blockNr<firstNonMetadataBlock)
			throw new FileSystemException("Attempt to free a filesystem metadata block!");

		byte[] bitmap = getBlock(gdesc.getBlockBitmap());
		
		//at any time, only one copy of the Block exists in the cache, so it is
		//safe to synchronize to the bitmapBlock object (it's part of Block)
		synchronized( bitmap ) {
			BlockBitmap.freeBit( bitmap, index );
			//update the bitmap block
			writeBlock(groupDescriptors[group].getBlockBitmap(), bitmap, false);
			//gdesc.setFreeBlocksCount(gdesc.getFreeBlocksCount()+1);
			modifyFreeBlocksCount(group, 1);
		}
	}
	
	
	/**
	 * Find free blocks in the block group <code>group</code>'s block bitmap. 
	 * First check for a whole byte of free blocks (0x00) in the bitmap, then 
	 * check for any free bit. If blocks are found, mark them as allocated.
	 * 
	 * @return	the index of the block (from the beginning of the partition)
	 * @param group the block group to check
	 * @param threshold 	find the free blocks only if there are at least
	 * 							<code>threshold</code> number of free blocks
	 */
	public BlockReservation findFreeBlocks(int group, long threshold) throws IOException{
		GroupDescriptor gdesc = groupDescriptors[group];
		//see if it's worth to check the block group at all
		if( gdesc.getFreeBlocksCount() < threshold)
			return new BlockReservation(false, -1, -1, gdesc.getFreeBlocksCount());
		
		/* Return false if the block is not a data block but a filesystem
		 * metadata block, as the beginning of each block group is filesystem
		 * metadata:
		 * superblock copy (if present)
		 * block bitmap
		 * inode bitmap
		 * inode table
		 * Free blocks begin after the inode table.
		 */
		long iNodeTableBlock = 	groupDescriptors[group].getInodeTable();
		long firstNonMetadataBlock = iNodeTableBlock + INodeTable.getSizeInBlocks(this);
		int  metadataLength  = (int)(firstNonMetadataBlock - 
								(superblock.getFirstDataBlock() + group*superblock.getBlocksPerGroup()));
		log.debug("group["+group+"].getInodeTable()="+iNodeTableBlock+", iNodeTable.getSizeInBlocks()="+INodeTable.getSizeInBlocks(this));
		log.debug("metadata length for block group("+group+"): "+metadataLength); 
		byte[] bitmapBlock = getBlock(gdesc.getBlockBitmap());

		//at any time, only one copy of the Block exists in the cache, so it is
		//safe to synchronize to the bitmapBlock object (it's part of Block)
		BlockReservation result;
		synchronized( bitmapBlock ) {
			result=BlockBitmap.findFreeBlocks( bitmapBlock, metadataLength );
		
			//if the reservation was successful, write the bitmap data to disk
			//within the same synchronized block
			if(result.isSuccessful()) {
				writeBlock(groupDescriptors[group].getBlockBitmap(), bitmapBlock, true);
				//gdesc.setFreeBlocksCount(gdesc.getFreeBlocksCount()-1-result.getPreallocCount());
				modifyFreeBlocksCount( group, -1-result.getPreallocCount() );
			}
		}
			
		if(result.isSuccessful()) {
			result.setBlock( group*getSuperblock().getBlocksPerGroup() + 
							 superblock.getFirstDataBlock() + 
							 result.getBlock() );
			result.setFreeBlocksCount(gdesc.getFreeBlocksCount());
		}
		
		return result;
	}
	
	/**
	 * Returns the number of groups.
	 * 
	 * @return int
	 */
	protected int getGroupCount() {
		return groupCount;
	}
	/**
	 * @return
	 */
	protected Object getGroupDescriptorLock() {
		return groupDescriptorLock;
	}

	/**
	 * @return
	 */
	protected Object getSuperblockLock() {
		return superblockLock;
	}

	/**
	 * With the sparse_super option set, a filesystem does not have a superblock
	 * copy in every block group.
	 * @param groupNr
	 * @return true if the block group <code>groupNr</code> has a superblock
	 */
	protected boolean groupHasSuperblock(int groupNr){
		//TODO: support filesystems with the sparse_super option
		return true; 
	}	
}
