package org.jnode.fs.hfsplus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HfsUnicodeStringTest {
	private byte[] STRING_AS_BYTES_ARRAY = new byte[]{0, 8, 0, 116, 0, 101, 0, 115, 0, 116, 0, 46, 0, 116, 0, 120, 0, 116};
	private String STRING_AS_TEXT = "test.txt";

	@Test
	public void testConstructAsBytesArray() {
		HfsUnicodeString string = new HfsUnicodeString(STRING_AS_BYTES_ARRAY,0);
		assertEquals(8,string.getLength());
		assertEquals(STRING_AS_TEXT,string.getUnicodeString());
	}
	
	@Test
	public void testConstructAsString() {
		HfsUnicodeString string = new HfsUnicodeString(STRING_AS_TEXT);
		assertEquals(8,string.getLength());
		byte[] array = string.getBytes();
		int index = 0;
		for (byte b : array) {
			assertEquals(STRING_AS_BYTES_ARRAY[index],b);
			index++;
		}
	}

}
