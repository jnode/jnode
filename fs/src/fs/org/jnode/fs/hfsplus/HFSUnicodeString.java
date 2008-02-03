package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;


public class HFSUnicodeString {

	private byte[] data;
	
	public HFSUnicodeString(byte[] src, int offset){
		data = new byte[src.length];
		System.arraycopy(src, offset, data, 0, src.length);
	}

	public int getLength(){
		return BigEndian.getInt16(data, 0);
	}
	
	public String getUnicodeString(){
		char[] result = new char[(getLength())];
		for(int i = 1; i < result.length; ++i){
		    result[i] = BigEndian.getChar(data, i*2);
		}
		return new String(result);
	}
	
}
