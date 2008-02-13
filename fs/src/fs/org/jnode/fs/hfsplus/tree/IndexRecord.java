package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class IndexRecord {
	private Key key;
	private final byte[] index;
	/**
	 * 
	 * @param key
	 * @param nodeData
	 * @param offset
	 */
	public IndexRecord(Key key,byte[] nodeData, int offset){
		this.key = key;
		index = new byte[4];
		System.arraycopy(nodeData, offset+key.getLength()+2, index, 0, 4);
	}
	
	public Key getKey(){
		return key;
	}
	
	public int getIndex(){
		return BigEndian.getInt32(index, 0);
	}
}
