package org.jnode.fs.ext2;

import java.io.IOException;

/**
 * @author Andras Nagy
 */
public class BlockBitmap extends FSBitmap {
	/**
	 * Test whether the block is free, and if yes:
	 * 	- mark it as used
	 *  - prealloc the next free blocks, at most 7
	 * 
	 * SYNCHRONIZATION:
	 * BlockBitmap.testAndSetBlock() is not synchronized, so 
	 * Ext2FileSystem.testAndSetBlock() is synchronized to the bitmap block
	 * it operates on.
	 */
	protected static BlockReservation testAndSetBlock(byte[] data, int index) throws IOException{
		if( isFree(data, index) ) {
			setBit(data, index);
			//do preallocation
			int j=0;
			while( (j<Ext2Constants.EXT2_PREALLOC_BLOCK) && isFree(data, index+1+j) ) {
				setBit(data, index+1+j);
				j++;
			}
			return new BlockReservation(true, index, j);	

		} else
			return new BlockReservation(false, -1, -1);
	}

	/**
	 * Find free blocks in the bitmap. First check
	 * for a whole byte of free blocks (0x00) in the bitmap, then check for 
	 * any free bit. If a block is found, mark it as allocated.
	 * If the following blocks are free, then they are preallocated (at most 7
	 * blocks), but preallocation can't be performed over the group boundary
	 * (because the bitmap data is only available for a single block group).
	 * 
	 * SYNCHRONIZATION:
	 * BlockBitmap.findFreeBlocks() is not synchronized, so 
	 * Ext2FileSystem.findFreeBlocks() is synchronized to the bitmap block
	 * it operates on.
	 */
	protected static BlockReservation findFreeBlocks(byte[] data, int metadataLength ) throws IOException{
		//BlockReservation result;
		int nonfullBitmap=-1;	//points to a nonfull byte in the bitmap
		
		//skip the metadata (superblock copy?, groupgrescriptor copies?, inode table)
		int first = (int)Math.ceil((double)metadataLength/8.0);
		
		for(int i=first; i<data.length; i++) {
			if(data[i]==0x00) {
				//allocate the block and do preallocation
				//preallocate a fixed number of blocks (7)
				data[i]=(byte)0xFF;
				return new BlockReservation(true, ((long)i)*8, 7);
			}
			
			if((nonfullBitmap==-1) && (data[i]!=0xFF))
				nonfullBitmap=i;
		}
	
		//a full byte of 0x00 was not found in the bitmap:
		//go for any free bit

		//	no free bit found:
		if(nonfullBitmap==-1)
			return new BlockReservation(false, -1, -1);
			
		//	a free bit has been found:
		for(int i=0; i<8; i++)
			if(isFree(data[nonfullBitmap], i)) {
				setBit(data, nonfullBitmap, i);
				int block = nonfullBitmap*8 + i;
				//do preallocation:
				int j=0;
				while( (j<Ext2Constants.EXT2_PREALLOC_BLOCK) && isFree(data, block+1+j) ) {
					setBit(data, block+1+j);
					j++;
				}
				return new BlockReservation(true, block, j);
			}
		
		return new BlockReservation(false, -1, -1);
	}

}
