package org.jnode.fs.hfsplus.catalog;

import static org.junit.Assert.assertEquals;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.junit.Test;

public class CatalogKeyTest {
	byte[] KEY_AS_BYTES_ARRAY = new byte[]{0,24,0,0,0,7,0, 8, 0, 116, 0, 101, 0, 115, 0, 116, 0, 46, 0, 116, 0, 120, 0, 116};
	String NODE_NAME_AS_STRING = "test.txt";

	@Test
	public void testKeyFromBytesArray(){
		CatalogKey key = new CatalogKey(KEY_AS_BYTES_ARRAY,0);
		assertEquals(NODE_NAME_AS_STRING,key.getNodeName().getUnicodeString());
		assertEquals(24,key.getKeyLength());
		assertEquals(7,key.getParentId().getId());
	}
	
	@Test
	public void testConstructFromCNIDAndString() {
		CatalogNodeId id = CatalogNodeId.HFSPLUS_START_CNID;
		HfsUnicodeString string = new HfsUnicodeString(NODE_NAME_AS_STRING);
		CatalogKey key = new CatalogKey(id,string);
		assertEquals(NODE_NAME_AS_STRING,key.getNodeName().getUnicodeString());
		assertEquals(24,key.getKeyLength());
		assertEquals(7,key.getParentId().getId());
		
	}

	@Test
	public void testConstructFromCNIDAndEmptyString() {
		CatalogNodeId id = CatalogNodeId.HFSPLUS_START_CNID;
		HfsUnicodeString string = new HfsUnicodeString("");
		CatalogKey key = new CatalogKey(id,string);
		assertEquals("",key.getNodeName().getUnicodeString());
		assertEquals(8,key.getKeyLength());
		assertEquals(7,key.getParentId().getId());
		
	}
	
}
