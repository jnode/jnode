/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.util.Date;

/**
 * @author vali
 */
public class NTFSUTIL {

	public static char READ16_CHAR(byte up, byte low)
	{
		char word = (char) low;
		word = (char) ((word << 8) | (up & 0xff));
		return word;
	}
	
	public static byte[] extractSubBuffer(byte[] buffer , int offset,int length)
	{
		byte[] temp = new byte[length];
		System.arraycopy(buffer,offset,temp,0,length);
		return temp;
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
