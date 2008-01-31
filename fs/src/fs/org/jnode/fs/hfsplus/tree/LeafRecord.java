package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class LeafRecord {
	private Key key;
	private final byte[] data;
	
	public LeafRecord(Key key, byte[] nodeData, int offset){
		this.key = key;
		data = new byte[2];
		System.arraycopy(nodeData, offset+key.getKeyLength(), data, 0, 2);
	}
	
	public Key getKey(){
		return key;
	}
	
	public int getType(){
		return BigEndian.getInt16(data, 0);
	}
	
	public String toString(){
		return "Type : " + getType() + "\nKey : " + getKey().toString() +  "\n";
	}
}
