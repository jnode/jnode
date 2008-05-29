package org.jnode.fs.hfsplus.extent;

import org.jnode.util.BigEndian;

public class ExtentDescriptor {

	public static final int EXTENT_DESCRIPTOR_LENGTH = 8;
	
	private byte[] data;
	
	public ExtentDescriptor(final byte[] src, final int offset){
		data = new byte[EXTENT_DESCRIPTOR_LENGTH];
		System.arraycopy(src, offset, data, 0, EXTENT_DESCRIPTOR_LENGTH);
	}
	
	public final int getStartBlock(){
		return BigEndian.getInt32(data, 0);
	}
	
	public final int getBlockCount(){
		return BigEndian.getInt32(data, 4);
	}
	
	public final String toString(){
		return "Start block : " + getStartBlock() + "\tBlock count : " + getBlockCount() + "\n"; 
	}
}
