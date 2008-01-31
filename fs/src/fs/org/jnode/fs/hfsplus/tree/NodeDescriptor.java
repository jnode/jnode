package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class NodeDescriptor {
	public final static int BT_NODE_DESCRIPTOR_LENGTH = 14;
	byte[] data;
	
	public NodeDescriptor(byte[] src){
		data = new byte[BT_NODE_DESCRIPTOR_LENGTH];
		System.arraycopy(src, 0, data, 0, BT_NODE_DESCRIPTOR_LENGTH);
	}
	
	public int getFLink(){
		return BigEndian.getInt32(data, 0);
	}
	
	public int getBLink(){
		return BigEndian.getInt32(data, 4);
	}
	
	public int getKind(){
		return BigEndian.getInt8(data, 8);
	}
	
	public int getHeight(){
		return BigEndian.getInt8(data, 9);
	}
	
	public int getNumRecords(){
		return BigEndian.getInt16(data, 10);
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("FLink  :" + getFLink()+ "\n");
		s.append("BLink  :" + getBLink() + "\n");
		s.append("Kind  :" + getKind() + "\n");
		s.append("height:" + getHeight() + "\n");
		s.append("#rec  :" + getNumRecords() + "\n");
		return s.toString();
	}
}
