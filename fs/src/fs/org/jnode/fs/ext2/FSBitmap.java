/*
 * $Id$
 */
package org.jnode.fs.ext2;

import org.jnode.fs.FileSystemException;

/**
 * This class provides static methods that operate on the data as a bitmap
 * 
 * @author Andras Nagy
 */
public class FSBitmap {
	private static final boolean DEBUG=true;	
	/**
	 * Check if the block/inode is free according to the bitmap.
	 * @param int index: the index of the block/inode relative to the block group 
	 * 		  (not relative to the whole fs)
	 * @return true if the block/inode is free, false otherwise
	 */
	protected static boolean isFree(byte[] data, int index) {
		int byteIndex  = index / 8;
		byte bitIndex  = (byte) (index % 8);
		byte mask = (byte) (1 << bitIndex);
		
		return ((data[byteIndex] & mask) == 0)? true : false;
	}

	protected static boolean isFree(byte data, int index) {
		//byte bitIndex  = (byte) (index % 8);
		
		byte mask = (byte) (1 << index);
		return ((data & mask) == 0)? true : false;
	}
		
	protected static void setBit(byte[] data, int index) {
		int byteIndex  = index / 8;
		byte bitIndex  = (byte) (index % 8);
		byte mask = (byte) (1 << bitIndex);
		
		data[byteIndex] = (byte) (data[byteIndex] |  mask);
	}
	
	protected static void setBit(byte[] data, int byteIndex, int bitIndex) {
		byte mask = (byte) (1 << bitIndex);
		
		data[byteIndex] = (byte) (data[byteIndex] |  mask);
	}
	
	protected static void freeBit(byte[] data, int index) throws FileSystemException{
		int byteIndex  = index  / 8;
		byte bitIndex  = (byte) (index % 8);
		byte mask = (byte) ~(1 << bitIndex);
		
		//filesystem consistency check
		if(DEBUG) {
			if(isFree(data[byteIndex], bitIndex))
				throw new FileSystemException("FS consistency error: you are trying " +
					"to free an unallocated block/inode");
		}		
		
		data[byteIndex] = (byte) (data[byteIndex] &  mask);
	}

}
