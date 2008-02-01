package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class LeafRecord {
	private Key key;
	private final byte[] recordData;
	
	public LeafRecord(Key key, byte[] nodeData, int offset, int recordDataSize){
		this.key = key;
		recordData = new byte[recordDataSize];
		System.arraycopy(nodeData, offset+key.getKeyLength()+2, recordData, 0, recordDataSize);
	}
	
	public Key getKey(){
		return key;
	}
	
	public int getType(){
		return BigEndian.getInt16(recordData, 0);
	}
	
	public byte[] getRecordData(){
		return recordData;
	}
	
	public String toString(){
		return "Type : " + getType() + "\nKey : " + getKey().toString() +  "\n";
	}
}
