package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.util.BigEndian;

public class CatalogThread {
	byte[] data;
	public CatalogThread(byte[] src){
			data = new byte[512];
			System.arraycopy(src, 0, data, 0, 512);
	}
	
	public int getRecordType(){
		return BigEndian.getInt16(data, 0);
	}
	
	public CatalogNodeId getParentId(){
		return new CatalogNodeId(data,4);
	}
	
	public HFSUnicodeString getNodeName(){
		return new HFSUnicodeString(data,8);
	}
}
