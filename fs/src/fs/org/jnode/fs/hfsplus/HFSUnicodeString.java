package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;


public class HFSUnicodeString {
	/** Length of string in characters. */
	private int length;
	
	private String string;
	
	public HFSUnicodeString(byte[] src, int offset){
		byte[] data = new byte[2];
		System.arraycopy(src, offset, data, 0, 2);
		length = BigEndian.getInt16(data, 0);
		data = new byte[length*2];
		System.arraycopy(src, offset+2, data, 0, length*2);
		char[] result = new char[length];
		for(int i = 0; i < length; ++i){
		    result[i] = BigEndian.getChar(data, i*2);
		}
		string =  new String(result);
	}

	public int getLength(){
		return length;
	}
	
	public String getUnicodeString(){
		return string;
	}
	
}
