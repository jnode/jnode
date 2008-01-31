package org.jnode.fs.hfsplus.catalog;

public class CatalogFile {
	private byte[] data;
	
	public CatalogFile(byte[] src){
		data = new byte[248];
		System.arraycopy(src, 0, data, 0, 248);
	}
}
