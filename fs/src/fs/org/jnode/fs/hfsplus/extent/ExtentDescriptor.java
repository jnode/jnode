package org.jnode.fs.hfsplus.extent;

import org.jnode.util.BigEndian;

public class ExtentDescriptor {

	public final static int EXTENT_DESCRIPTOR_LENGTH = 8;
	
	private byte data[];
	
	public ExtentDescriptor(byte[] src, int offset){
		data = new byte[EXTENT_DESCRIPTOR_LENGTH];
		System.arraycopy(src, offset, data, 0, EXTENT_DESCRIPTOR_LENGTH);
	}
	
	public int getStartBlock(){
		return BigEndian.getInt32(data, 0);
	}
	
	public int getBlockCount(){
		return BigEndian.getInt32(data, 4);
	}
	
	public String toString(){
		return "Start block : " + getStartBlock() + "\tBlock count : " + getBlockCount() + "\n"; 
	}
}
