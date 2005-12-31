/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
