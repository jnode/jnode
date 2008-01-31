package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;

public class JournalInfoBlock {
	private byte[] data;
	public JournalInfoBlock(byte[] src){
		data = new byte[180];
		System.arraycopy(src, 0, data, 0, 180);
	}
	
	public int getFlag(){
		return BigEndian.getInt32(data, 0);
	}
	
	public long getOffset(){
		return BigEndian.getInt64(data, 36);
	}
	
	public long getSize(){
		return BigEndian.getInt64(data, 44);
	}
	
	public String toString(){
		return "Journal : " + getOffset() + "::" + getSize();
	}
}
