/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.jnode.fs.FileSystemException;

/**
 * @author Andras Nagy
 */
public class INodeTable {
	private final int blockSize;
	int blockCount;
	Ext2FileSystem fs;
	int firstBlock;
	
	public INodeTable(Ext2FileSystem fs, int firstBlock) throws IOException {
		this.fs = fs;
		this.firstBlock = firstBlock;
		blockSize=fs.getSuperblock().getBlockSize();
		blockCount = (int)Math.ceil(
			(double)(fs.getSuperblock().getINodesCount()*INode.INODE_LENGTH) / 
			(double) blockSize);
	}
	
	public byte[] getINodeTableBlock(int blockNo) throws FileSystemException, IOException{
		if(blockNo < blockCount) 
			return fs.getBlock(firstBlock+blockNo);
		else throw new FileSystemException("Trying to get block #"+blockNo+
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
}
