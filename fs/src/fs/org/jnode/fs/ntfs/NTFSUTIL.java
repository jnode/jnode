/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.util.Date;

/**
 * @author vali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSUTIL {

	public static int makeWORDfrom2Bytes(byte[] values)
	{
		int word = values[1];
		word = (word << 8) | (values[0] & 0xff);
		return word;
	}

	public static int makeWORDfrom2Bytes(byte up, byte low)
	{
		int word = low;
		word = (word << 8) | (up & 0xff);
		return word;
	}
	public static char makeCHARfrom2Bytes(byte up, byte low)
	{
		char word = (char) low;
		word = (char) ((word << 8) | (up & 0xff));
		return word;
	}
	public static long get32(byte[] data, int offset) {
		int b1 = data[offset] & 0xFF;
		int b2 = data[offset+1] & 0xFF;
		int b3 = data[offset+2] & 0xFF;
		int b4 = data[offset+3] & 0xFF;
		return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
	}
	
	public static long ARRAY2LONG (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 7] & 0xFF;
		int b2 = abArray[offset + 6] & 0xFF;
		int b3 = abArray[offset + 5] & 0xFF;
		int b4 = abArray[offset + 4] & 0xFF;
		int b5 = abArray[offset + 3] & 0xFF;
		int b6 = abArray[offset + 2] & 0xFF;
		int b7 = abArray[offset + 1] & 0xFF;
		int b8 = abArray[offset + 0] & 0xFF;
		
		return (
				(b1 << 56) | 
				(b2 << 48) | 
				(b3 << 40) | 
				(b4 << 32) | 
				(b5 << 24) | 
				(b6 << 16) | 
				(b7 << 8) | 
				b8);
	}

	public static byte[] extractSubBuffer(byte[] buffer , int offset,int length)
	{
		byte[] temp = new byte[length];
		System.arraycopy(buffer,offset,temp,0,length);
		return temp;
	}

	public static int ARRAY2INT (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 3] & 0x000000FF;
		int b2 = abArray[offset + 2] & 0x000000FF;
		int b3 = abArray[offset + 1] & 0x000000FF;
		int b4 = abArray[offset + 0] & 0x000000FF;
		
		return (
				(b1 << 24) | 
				(b2 << 16) | 
				(b3 << 8) | 
				b4) & 0xFFFFFFFF;
	}
	public static Date getDateForNTFSTimes(long _100ns)
	{
		Date date = new Date((_100ns /  (10 * 1000)) - ((1000 * 60 * 60 *24) * 134774));
		/*System.out.println(date);
		System.out.println(_100ns);
		System.out.println(((1000 * 60 * 60 *24) * 134774));*/
		return date;
	}
}
