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

	public static int LE_READ_U16_INT(byte up, byte low)
	{
		int word = low;
		word = (word << 8) | (up & 0xff);
		return word & 0xFFFFFFFF;
	}
	
	public static int LE_READ_16_INT(byte up, byte low)
	{
		int word = low;
		word = (word << 8) | (up & 0xff);
		return word;
	}
	
	public static char READ16_CHAR(byte up, byte low)
	{
		char word = (char) low;
		word = (char) ((word << 8) | (up & 0xff));
		return word;
	}
	
	public static long LE_READ_U64_LONG (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 7] & 0xFF;
		int b2 = abArray[offset + 6] & 0xFF;
		int b3 = abArray[offset + 5] & 0xFF;
		int b4 = abArray[offset + 4] & 0xFF;
		int b5 = abArray[offset + 3] & 0xFF;
		int b6 = abArray[offset + 2] & 0xFF;
		int b7 = abArray[offset + 1] & 0xFF;
		int b8 = abArray[offset + 0] & 0xFF;
		
		return 
				(b1 << 56) | 
				(b2 << 48) | 
				(b3 << 40) | 
				(b4 << 32) | 
				(b5 << 24) | 
				(b6 << 16) | 
				(b7 << 8) | 
				b8;
	}

	public static byte[] extractSubBuffer(byte[] buffer , int offset,int length)
	{
		byte[] temp = new byte[length];
		System.arraycopy(buffer,offset,temp,0,length);
		return temp;
	}

	public static int LE_READ_U32_INT (byte[] abArray, int offset)
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
	public static int LE_READ_U48_INT (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 5] & 0x000000FF;
		int b2 = abArray[offset + 4] & 0x000000FF;
		int b3 = abArray[offset + 3] & 0x000000FF;
		int b4 = abArray[offset + 2] & 0x000000FF;
		int b5 = abArray[offset + 1] & 0x000000FF;
		int b6 = abArray[offset + 0] & 0x000000FF;
		
		return (
				(b1 << 40) | 
				(b2 << 32) | 
				(b3 << 24) | 
				(b4 << 16) | 
				(b5 << 8) | 
				b6) & 0xFFFFFFFF;
	}
	public static int LE_READ_32_INT (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 3];
		int b2 = abArray[offset + 2];
		int b3 = abArray[offset + 1];
		int b4 = abArray[offset + 0];
		
		return 
				(b1 << 24) | 
				(b2 << 16) | 
				(b3 << 8) | 
				 b4;
	}
	public static int LE_READ_24_INT (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 2] & 0x000000FF;
		int b2 = abArray[offset + 1] & 0x000000FF;
		int b3 = abArray[offset + 0] & 0x000000FF;
		
		return (
				(b1 << 16) | 
				(b2 << 8) | 
				b3);
	}
	
	public static int LE_READ_U24_INT (byte[] abArray, int offset)
	{
		int b1 = abArray[offset + 2] & 0x000000FF;
		int b2 = abArray[offset + 1] & 0x000000FF;
		int b3 = abArray[offset + 0] & 0x000000FF;
		
		return (
				(b1 << 16) | 
				(b2 << 8) | 
				 b3) & 0xFFFFFFFF;
	}
	public static Date getDateForNTFSTimes(long _100ns)
	{
		long timeoffset = Math.abs((369*365 + 89) * 24 * 3600 * 10000000);
		long time = (Math.abs(_100ns) - timeoffset);
		
		System.out.println("hours" + ((Math.abs(time) / 1000)  / 60)/60);
		Date date = new Date(time);
		System.out.println(date.toLocaleString());
		return date;
	}
	public static void printOutHexLong(String name,long value)
	{
		System.out.println(name + " = 0x"  + Long.toHexString(value));
	}
}
