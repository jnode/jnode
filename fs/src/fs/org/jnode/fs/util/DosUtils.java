/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.util;

import java.util.Calendar;

/**
 * <description>
 * 
 * @author epr
 */
public class DosUtils {

	/**
	 * Gets an unsigned 8-bit byte from a given offset
	 * @param offset
	 * @return int
	 */
	public static int get8(byte[] data, int offset) {
		return data[offset] & 0xFF;
	}

	/**
	 * Sets an unsigned 8-bit byte at a given offset
	 * @param offset
	 */
	public static  void set8(byte[] data, int offset, int value) {
		data[offset] = (byte)(value & 0xFF);
	}

	/**
	 * Gets an unsigned 16-bit word from a given offset
	 * @param offset
	 * @return int
	 */	
	public static  int get16(byte[] data, int offset) {
		int b1 = data[offset] & 0xFF;
		int b2 = data[offset+1] & 0xFF;
		return (b2 << 8) | b1;
	}
	
	/**
	 * Sets an unsigned 16-bit word at a given offset
	 * @param offset
	 */
	public static void set16(byte[] data, int offset, int value) {
		data[offset] = (byte)(value & 0xFF);
		data[offset+1] = (byte)((value >> 8) & 0xFF);
	}

	/**
	 * Gets an unsigned 32-bit word from a given offset
	 * @param offset
	 * @return int
	 */	
	public static  long get32(byte[] data, int offset) {
		long b1 = data[offset] & 0xFF;
		long b2 = data[offset+1] & 0xFF;
		long b3 = data[offset+2] & 0xFF;
		long b4 = data[offset+3] & 0xFF;
		return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
	}
	
	/**
	 * Sets an unsigned 32-bit word at a given offset
	 * @param offset
	 */
	public static void set32(byte[] data, int offset, long value) {
		data[offset] = (byte)(value & 0xFF);
		data[offset+1] = (byte)((value >> 8) & 0xFF);
		data[offset+2] = (byte)((value >> 16) & 0xFF);
		data[offset+3] = (byte)((value >> 24) & 0xFF);
	}
	
	/**
	 * Decode a 16-bit encoded DOS date/time into a java date/time.
	 * @param dosTime
	 * @return long
	 */
	public static long decodeDateTime(int dosDate, int dosTime) {
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.SECOND, (dosTime & 0x1f) * 2);
		cal.set(Calendar.MINUTE, (dosTime >> 5) & 0x3f);
		cal.set(Calendar.HOUR, dosTime >> 11);
		
		cal.set(Calendar.DATE, dosDate & 0x1f);
		cal.set(Calendar.MONTH, (dosDate >> 5) & 0x0f);
		cal.set(Calendar.YEAR, 1980 + (dosDate >> 9));
		
		return cal.getTimeInMillis();
	}

	/**
	 * Encode a java date/time into a 16-bit encoded DOS time
	 * @param javaDateTime
	 * @return long
	 */
	public static int encodeTime(long javaDateTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(javaDateTime);
		
		return 2048 * cal.get(Calendar.HOUR) +
		        32 * cal.get(Calendar.MINUTE) +
		        cal.get(Calendar.SECOND) / 2;
	}

	/**
	 * Encode a java date/time into a 16-bit encoded DOS date
	 * @param javaDateTime
	 * @return long
	 */
	public static int encodeDate(long javaDateTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(javaDateTime);
		
		return 512 * (cal.get(Calendar.YEAR) - 1980) +
				32 * cal.get(Calendar.MONTH) +
				cal.get(Calendar.DATE);
	}
}
