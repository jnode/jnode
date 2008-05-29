package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class IndexRecord {
	private final Key key;
	private final byte[] index;
	/**
	 * 
	 * @param key
	 * @param nodeData
	 * @param offset
	 */
	public IndexRecord(final Key key,final byte[] nodeData, final int offset){
		this.key = key;
		index = new byte[4];
		System.arraycopy(nodeData, offset+key.getLength()+2, index, 0, 4);
	}
	
	public final Key getKey(){
		return key;
	}
	
	public final int getIndex(){
		return BigEndian.getInt32(index, 0);
	}
}
