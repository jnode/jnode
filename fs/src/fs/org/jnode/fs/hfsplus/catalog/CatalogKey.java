package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.util.BigEndian;

public class CatalogKey extends AbstractKey {
	private int keyLength;
	private CatalogNodeId parentID;
	private String nodeName;
	
	public CatalogKey(byte[] src, int offset){
		byte[] ck = new byte[2];
		System.arraycopy(src, offset, ck, 0, 2);
		keyLength = BigEndian.getInt16(ck, 0);
		ck = new byte[4];
		System.arraycopy(src, offset+2, ck, 0, 4);
		parentID = new CatalogNodeId(ck,0);
		if(keyLength > 6){
			ck = new byte[keyLength-6];
			System.arraycopy(src, offset+6, ck, 0, keyLength - 6);
			nodeName = new HFSUnicodeString(ck,0).getUnicodeString();
		}
	}
	
	public int getKeyLength(){
		return keyLength;
	}
	
	public int getLength(){
		return keyLength;
	}
	
	public CatalogNodeId getParentId(){
		return parentID;
	}
	
	public String getNodeName(){
		return nodeName;
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Key length:").append(getKeyLength()).append("\t");
		s.append("Parent ID :").append(getParentId().getId()).append("\t");
		s.append("Node name :").append(getNodeName()).append("\n");
		return s.toString();
	}
}
