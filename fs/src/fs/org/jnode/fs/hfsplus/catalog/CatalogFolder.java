package org.jnode.fs.hfsplus.catalog;

public class CatalogFolder {
	private byte[] data;
	
	public CatalogFolder(byte[] src){
		data = new byte[88];
		System.arraycopy(src, 0, data, 0, 88);
	}
}
