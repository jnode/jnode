/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.jnode.fs.FileSystemException;

/**
 * This class represents a part of the inode table (that which is contained
 * in one block group).
 * 
 * It provides methods for reading and writing the (already allocated) inodes.
 * 
 * @author Andras Nagy
 */
public class INodeTable {
	private final int blockSize;
	int blockCount;
	Ext2FileSystem fs;
	int firstBlock;		//the first block of the inode table
	
	public INodeTable(Ext2FileSystem fs, int firstBlock) throws IOException {
		this.fs = fs;
		this.firstBlock = firstBlock;
		blockSize=(int)fs.getBlockSize();
		blockCount = (int)Math.ceil(
			(double)(fs.getSuperblock().getINodesPerGroup()*INode.INODE_LENGTH) / 
			(double) blockSize);
	}
	
	public static int getSizeInBlocks(Ext2FileSystem fs) {
		int count = (int)Math.ceil(
			(double)(fs.getSuperblock().getINodesPerGroup()*INode.INODE_LENGTH) / 
			(double) fs.getBlockSize());		
		return count;
	}
	
	/**
	 * get the <code>blockNo</code>th block from the beginning of the inode table
	 * @param blockNo
	 * @return the contents of the block as a byte[]
	 * @throws FileSystemException
	 * @throws IOException
	 */
	public byte[] getINodeTableBlock(int blockNo) throws FileSystemException, IOException{
		if(blockNo < blockCount) 
			return fs.getBlock(firstBlock+blockNo);
		else throw new FileSystemException("Trying to get block #"+blockNo+
			"of an inode table that only has "+blockCount+" blocks");
	}
	
	/**
	 * Write the <code>blockNo</code>th block (from the beginning of the inode table) 
	 * @param data
	 * @param blockNo
	 * @throws FileSystemException
	 * @throws IOException
	 */
	public void writeINodeTableBlock(byte[] data, int blockNo) throws FileSystemException, IOException {
		if(blockNo < blockCount) 
			fs.writeBlock(firstBlock+blockNo, data, false);
		else throw new FileSystemException("Trying to write block #"+blockNo+
			"of an inode table that only has "+blockCount+" blocks");		
	}
	
	/** 
	 * Get the indexth inode from the inode table.
	 * (index is not an inode number, it is just an index in the inode table)
	 */
	public byte[] getInodeData(int index) throws IOException, FileSystemException{
		byte data[] = new byte[INode.INODE_LENGTH];
		
		int indexCopied = 0;
		while(indexCopied<INode.INODE_LENGTH) {
			int blockNo 	= (index*INode.INODE_LENGTH+indexCopied) / blockSize;
			int blockOffset = (index*INode.INODE_LENGTH+indexCopied) % blockSize;
			int copyLength 	= Math.min(blockSize-blockOffset, INode.INODE_LENGTH);
			System.arraycopy(	getINodeTableBlock(blockNo), blockOffset, 
								data, indexCopied, 
								copyLength);
			indexCopied+=copyLength;
		}
		return data;
	}
	
	public void writeInodeData(int index, byte[] data) throws IOException, FileSystemException {
		int indexCopied = 0;
		while(indexCopied<INode.INODE_LENGTH) {
			int blockNo 	= (index*INode.INODE_LENGTH+indexCopied) / blockSize;
			int blockOffset = (index*INode.INODE_LENGTH+indexCopied) % blockSize;
			int copyLength 	= Math.min(blockSize-blockOffset, INode.INODE_LENGTH);
			byte[] originalBlock = getINodeTableBlock(blockNo);
			System.arraycopy( 	data, indexCopied, 
								originalBlock, blockOffset, 
								copyLength);
			indexCopied+=copyLength;
			writeINodeTableBlock(originalBlock, blockNo);
		}
		
	}
}
