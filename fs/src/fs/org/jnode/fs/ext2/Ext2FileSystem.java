package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.HashMap;

import org.jnode.driver.ide.IDEDiskPartitionDriver;
import org.jnode.driver.Device;
import org.jnode.driver.Driver;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ext2.cache.Block;
import org.jnode.fs.ext2.cache.BlockCache;

/**
 * @author Andras Nagy
 * 
 */
public class Ext2FileSystem implements FileSystem {
	/* remarks about the code:
	 * -wherever the number (count) of some structure is known (e.g. number of
	 * 	block groups), I use an array to store them
	 */
	private final Device device;
	//private final BlockDeviceAPI api;
	private IDEDiskPartitionDriver pdriver;
	private boolean closed;
	private Superblock superblock;
	private GroupDescriptor groupDescriptors[];
	private int groupCount;
	private BlockCache cache;
	
	/**
	 * Constructor for Ext2FileSystem.
	 */
	public Ext2FileSystem(Device device) throws FileSystemException{
		if(device==null)
			throw new FileSystemException("null device!");
			
		this.device = device;
		closed = false;
		byte data[];
		//points to the byte where the fs metadata parsing has reached
		long byteIndex;		
		
		cache = new BlockCache(50,(float)0.75);

		Driver driver = device.getDriver();
		if(driver instanceof IDEDiskPartitionDriver) 
			pdriver = (IDEDiskPartitionDriver)driver;

		try {
			data = new byte[Superblock.SUPERBLOCK_LENGTH];

			//skip the first 1024 bytes (bootsector) and read the superblock 
			byteIndex = 1024;
			pdriver.read(byteIndex, data, 0, Superblock.SUPERBLOCK_LENGTH);
			byteIndex+=Superblock.SUPERBLOCK_LENGTH;
			superblock = new Superblock(data);
			
			//read the group descriptors
			groupCount = (int)Math.ceil((double)superblock.getBlocksCount() / (double)superblock.getBlocksPerGroup());
			groupDescriptors = new GroupDescriptor[groupCount];
			for(int i=0; i<groupCount; i++) {
				data = new byte[GroupDescriptor.GROUPDESCRIPTOR_LENGTH];
				pdriver.read(	byteIndex+i*GroupDescriptor.GROUPDESCRIPTOR_LENGTH, 
							data, 0, 
							GroupDescriptor.GROUPDESCRIPTOR_LENGTH);
				
				groupDescriptors[i] = new GroupDescriptor(data);
			}	
			byteIndex += superblock.getBlockSize();
			
			/*
			//go through each block group
			//XXX byteIndex = ...		
				//read the block bitmap
				//XXX what if it doesn't fit?
				data = new byte[(int)superblock.getBlockSize()];
				api.read( byteIndex, data, 0, (int)superblock.getBlockSize());
				FSBitmap blockBitmap = new FSBitmap(data);
				byteIndex += superblock.getBlockSize();
			
				//read the inode bitmap
				//XXX what if it doesn't fit?
				data = new byte[(int)superblock.getBlockSize()];
				api.read( byteIndex, data, 0, (int)superblock.getBlockSize());
				FSBitmap inodeBitmap = new FSBitmap(data);
				byteIndex += superblock.getBlockSize();
			
				//read the inode table
				//XXX what if it doesn't fit?
				Inode inodeTable[] = new Inode[ (int)superblock.getInodesCount() ];
			*/
		} catch (FileSystemException e) {
			throw e;
		} catch (Exception e) {
			throw new FileSystemException(e);
		}		
		
		Ext2Debugger.debug( "Ext2fs filesystem constructed sucessfully");
		Ext2Debugger.debug( "	superblock:	#blocks:		"+superblock.getBlocksCount()+"\n"+
							"				#blocks/group:	"+superblock.getBlocksPerGroup()+"\n"+
							"				#block groups:	"+groupCount+"\n"+
							"				block size:		"+superblock.getBlockSize()+"\n"+
							"				#inodes:		"+superblock.getINodesCount()+"\n"+
							"				#inodes/group:	"+superblock.getINodesPerGroup());
	}

	/**
	 * @see org.jnode.fs.FileSystem#close()
	 */
	public void close() throws IOException {
		flush();
		closed = true;
		//XXX
		throw new IOException("Yet unimplemented");
	}

	/**
	 * Flush all changed structures to the device.
	 * @throws IOException
	 */
	public void flush() throws IOException {

		//final BlockDeviceAPI api = this.api;
		//XXX
		throw new IOException("Yet unimplemented");
	}

	/**
	 * @see org.jnode.fs.FileSystem#getDevice()
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @see org.jnode.fs.FileSystem#getRootEntry()
	 */
	public FSEntry getRootEntry() throws IOException {
		Ext2Debugger.debug("Ext2FileSystem.getRootEntry()",2);
		try{
			if(!closed) {
				return new Ext2Entry( getINode(Ext2Constants.EXT2_ROOT_INO), "/", Ext2Constants.EXT2_FT_DIR );
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
	 * Return a data block
	 */
	public byte[] getBlock(long nr) throws IOException{
		int blockSize = superblock.getBlockSize();
		Block result;
		
		Integer key=new Integer((int)nr);
		//check if the block has already been retrieved
		if(cache.containsKey(key)) 
			result=(Block)cache.get(key);
		else{
			byte[] data = new byte[blockSize];
			pdriver.read( nr*blockSize, data, 0, blockSize);
			result=new Block(data);
			cache.put(key, result);
		}
			
		return result.getData();
	}
	
	public Superblock getSuperblock() {
		return superblock;
	}	

	/** 
	 * Return the inode numbered inodeNr (the first inode is #1)
	 */
	public INode getINode(int iNodeNr) throws IOException, FileSystemException{
		if(iNodeNr<1)
			throw new IOException("INode number must be greater than 0");
			
		int group = (int) ((iNodeNr-1) / superblock.getINodesPerGroup());
		int index = (int) ((iNodeNr - 1) % superblock.getINodesPerGroup());

		//get the part of the inode table that contains the inode
		long iNodeTableBlock  = groupDescriptors[group].getInodeTable();	//the first block of the inode table
		INodeTable iNodeTable = new INodeTable(this, (int)iNodeTableBlock);
		return new INode(this, iNodeTable.getInodeData(index));		
	}
	

}
