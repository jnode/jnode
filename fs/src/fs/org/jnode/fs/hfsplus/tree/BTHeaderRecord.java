package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class BTHeaderRecord {
	public final static int BT_HEADER_RECORD_LENGTH = 106;
	byte[] data;
	
	public BTHeaderRecord(byte[] src){
		data = new byte[BT_HEADER_RECORD_LENGTH];
		System.arraycopy(src, 0, data, 0, BT_HEADER_RECORD_LENGTH);
	}
	
	public int getTreeDepth(){
		return BigEndian.getInt16(data, 0);
	}
	
	public int getRootNode(){
		return BigEndian.getInt32(data, 2);
	}
	
	public int getLeafRecords(){
		return BigEndian.getInt32(data, 6);
	}
	public int getFirstLeafNode(){
		return BigEndian.getInt32(data, 10);
	}
	public int getLastLeafNode(){
		return BigEndian.getInt32(data, 14);
	}
	public int getNodeSize(){
		return BigEndian.getInt16(data, 18);
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Root node  :" + getRootNode() + "\n");
		s.append("First leaf :" + getFirstLeafNode() + "\n");
		s.append("Last leaf  :" + getLastLeafNode() + "\n");
		s.append("node size  :" + getNodeSize() + "\n");
		return s.toString();
	}
}
