package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class CatalogKey extends AbstractKey {
	private int keyLength;
	private CatalogNodeId parentID;
	private HFSUnicodeString nodeName;
	/**
	 * 
	 * @param src
	 * @param offset
	 */
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
			nodeName = new HFSUnicodeString(ck,0);
		}
	}
	/**
	 * 
	 * @param parentID
	 * @param name
	 */
	public CatalogKey(CatalogNodeId parentID, HFSUnicodeString name){
		this.parentID = parentID;
		this.nodeName = name;
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
	
	public HFSUnicodeString getNodeName(){
		return nodeName;
	}
	
	public int compareTo(Key o) {
		if(o instanceof CatalogKey) {
			CatalogKey ck = (CatalogKey) o;
			if(getParentId().getId() == ck.getParentId().getId())
				return nodeName.getUnicodeString().compareTo(ck.getNodeName().getUnicodeString());
			else if(getParentId().getId() < ck.getParentId().getId())
				return -1;
			else
				return 1;
		} else {
			return -1;
		}
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Key length:").append(getKeyLength()).append("\t");
		s.append("Parent ID :").append(getParentId().getId()).append("\t");
		s.append("Node name :").append(getNodeName().getUnicodeString()).append("\n");
		return s.toString();
	}
}
