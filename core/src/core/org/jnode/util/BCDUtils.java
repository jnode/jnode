/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class BCDUtils {
	
	/**
	 * Convert a BCD encoded value into a (normal) binary value
	 * @param bcd
	 * @return int
	 */
	public static int bcd2bin(int bcd) {
		return (bcd & 15) + ((bcd >> 4) * 10);
	}
	
	/**
	 * Convert a (normal) binary value into a BCD encoded value.
	 * @param bin
	 * @return int 
	 */
	public static int bin2bcd(int bin) {
		return ((bin / 10) << 4) + (bin % 10);
	}
}
