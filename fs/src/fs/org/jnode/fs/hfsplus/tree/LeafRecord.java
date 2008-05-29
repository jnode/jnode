package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class LeafRecord {
	private final Key key;
	private final byte[] recordData;
	
	public LeafRecord(final Key key, final byte[] nodeData, final int offset, final int recordDataSize){
		this.key = key;
		recordData = new byte[recordDataSize];
		System.arraycopy(nodeData, offset+key.getKeyLength()+2, recordData, 0, recordDataSize);
	}
	
	public final Key getKey(){
		return key;
	}
	
	public final int getType(){
		return BigEndian.getInt16(recordData, 0);
	}
	
	public final byte[] getRecordData(){
		return recordData;
	}
	
	public final String toString(){
		return "Type : " + getType() + "\nKey : " + getKey().toString() +  "\n";
	}
}
