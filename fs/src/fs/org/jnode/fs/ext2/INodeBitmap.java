/* $Id$
 */
package org.jnode.fs.ext2;

/**
 * @author Andras Nagy
 */
public class INodeBitmap extends FSBitmap {
	/**
	 * Test whether the inode is free, and if yes, mark it as used
	 * 
	 * SYNCHRONIZATION:
	 * INodeBitmap.testAndSetINode() is not synchronized, so 
	 * Ext2FileSystem.createINode() is synchronized to the bitmap block
	 * it operates on.
	 */
	protected static boolean testAndSetINode(byte[] data, int index) {
		if( isFree(data, index) ) {
			setBit(data, index);
			return true;	
		} else
			return false;
	}
	
	public static INodeReservation findFreeINode(byte[] bitmapBlock) {
		for(int i=0; i<bitmapBlock.length; i++) {
			if(bitmapBlock[i]!=0xFF) {
				for(int j=0; j<8; j++){
					if(isFree(bitmapBlock[i],j)) {					
						setBit(bitmapBlock,i,j);
						return new INodeReservation(true, i*8+j);
					}
				}
			}
		}
		return new INodeReservation(false, -1);
	}

}
