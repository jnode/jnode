package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class ExtentKey extends AbstractKey {
	
    public static final byte DATA_FORK = (byte)0x00;
    public static final byte RESOURCE_FORK = (byte)0xFF;
	public final static int KEY_LENGTH = 12;

	byte[] ek;
	
	public ExtentKey(byte[] src, int offset){
		ek = new byte[KEY_LENGTH];
		System.arraycopy(src, offset, ek, 0, KEY_LENGTH);
	}
	
	@Override
	public int getKeyLength() {
		return BigEndian.getInt16(ek, 0);
	}
	
    public int getForkType(){
		return BigEndian.getInt8(ek, 2);
	}
	
	public int getPad(){
		return BigEndian.getInt8(ek, 3);
	}
	
	public CatalogNodeId getCatalogNodeId(){
		return new CatalogNodeId(ek,4);
	}
	
	public int getStartBlock(){
		return BigEndian.getInt32(ek, 8);
	}
    
	@Override
	public int getLength() {
		return KEY_LENGTH;
	}

	@Override
	public int compareTo(Key key) {
		// TODO Auto-generated method stub
		return 0;
	}

}
