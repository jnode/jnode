package org.jnode.fs.hfsplus.catalog;

import org.jnode.util.BigEndian;

public class CatalogFolder {
	private byte[] data;
	
	public CatalogFolder(byte[] src){
		data = new byte[88];
		System.arraycopy(src, 0, data, 0, 88);
	}
	
	public int getRecordType(){
		return BigEndian.getInt16(data, 0);
	}
	
	public int getValence(){
		return BigEndian.getInt32(data, 4);
	}
	
	public CatalogNodeId getFolderId(){
		return new CatalogNodeId(data,8);
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Record type:").append(getRecordType()).append("\t");
		s.append("Valence    :").append(getValence()).append("\t");
		s.append("Folder ID  :").append(getFolderId().getId()).append("\n");
		return s.toString();
	}
}
