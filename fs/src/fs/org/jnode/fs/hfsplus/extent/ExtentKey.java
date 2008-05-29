package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class ExtentKey extends AbstractKey {
	
    public static final byte DATA_FORK = (byte)0x00;
    public static final byte RESOURCE_FORK = (byte)0xFF;
	public static final int KEY_LENGTH = 12;

	byte[] ek;
	
	public ExtentKey(final byte[] src, final int offset){
		ek = new byte[KEY_LENGTH];
		System.arraycopy(src, offset, ek, 0, KEY_LENGTH);
	}
	
	@Override
	public final int getKeyLength() {
		return BigEndian.getInt16(ek, 0);
	}
	
    public final int getForkType(){
		return BigEndian.getInt8(ek, 2);
	}
	
	public final int getPad(){
		return BigEndian.getInt8(ek, 3);
	}
	
	public final CatalogNodeId getCatalogNodeId(){
		return new CatalogNodeId(ek,4);
	}
	
	public final int getStartBlock(){
		return BigEndian.getInt32(ek, 8);
	}
    
	@Override
	public final int getLength() {
		return KEY_LENGTH;
	}

	@Override
	public final int compareTo(final Key key) {
		// TODO Auto-generated method stub
		return 0;
	}

}
