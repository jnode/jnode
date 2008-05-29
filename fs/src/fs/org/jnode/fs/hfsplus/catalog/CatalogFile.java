package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSPlusForkData;
import org.jnode.util.BigEndian;

public class CatalogFile {
	private byte[] data;
	
	public CatalogFile(final byte[] src){
		data = new byte[248];
		System.arraycopy(src, 0, data, 0, 248);
	}
	
	public final int getRecordType(){
		return BigEndian.getInt16(data, 0);
	}
	
	public final CatalogNodeId getFileId(){
		return new CatalogNodeId(data,8);
	}
	
	public final HFSPlusForkData getDataFork(){
		return new HFSPlusForkData(data,88);
	}
	
	public final HFSPlusForkData getResourceFork(){
		return new HFSPlusForkData(data,168);
	}
	
	public final String toString(){
		StringBuffer s = new StringBuffer();
		s.append("Record type:").append(getRecordType()).append("\t");
		s.append("File ID  :").append(getFileId().getId()).append("\n");
		return s.toString();
	}
}
