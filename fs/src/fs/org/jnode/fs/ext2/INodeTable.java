package org.jnode.fs.ext2;

import java.io.IOException;

/**
 * @author Andras Nagy
 */
public class INodeTable {
	byte[][] iNodeTable;
	private final int blockSize;
	
	public INodeTable(Ext2FileSystem fs, int firstBlock) throws IOException {
		blockSize=fs.getSuperblock().getBlockSize();
		int blockCount = (int)Math.ceil(
			(double)(fs.getSuperblock().getINodesCount()*INode.INODE_LENGTH) / 
			(double) blockSize);
		iNodeTable = new byte[blockCount][];
		for(int i=0; i<blockCount; i++) {
			iNodeTable[i] = fs.getBlock(firstBlock);
		}
	}
	
	/** 
	 * Get the indexth inode from the inode table.
	 * (index is not an inode number, it is just an index in the inode table)
	 */
	public byte[] getInodeData(int index) {
		byte data[] = new byte[INode.INODE_LENGTH];
		
		int indexCopied = 0;
		while(indexCopied<INode.INODE_LENGTH) {
			int blockNo 	= (index*INode.INODE_LENGTH+indexCopied) / blockSize;
			int blockOffset = (index*INode.INODE_LENGTH+indexCopied) % blockSize;
			int copyLength 	= Math.min(blockSize-blockOffset, INode.INODE_LENGTH);
			System.arraycopy(	iNodeTable[blockNo], blockOffset, 
								data, indexCopied, 
								copyLength);
			indexCopied+=copyLength;
		}
		return data;
	}
}
