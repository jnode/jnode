/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.util;

/**
 * @author epr
 */
public class NumberUtils {
	
    public static final int K = 1024;
    public static final int M = 1024*1024;
    public static final int G = 1024*1024*1024;
    public static final long T = 1024l*1024l*1024l*1024l;
    
    /**
     * Convert a float to a string with a given maximum number of fraction digits.
     * @param value
     * @param maxFractionLength
     * @return The string 
     */
    public static String toString(float value, int maxFractionLength) {
        String s = Float.toString(value);
        final int idx = s.indexOf('.');
        if (idx >= 0) {
            final int len = Math.min(s.length(), idx + maxFractionLength +  1);
            return s.substring(0, len);
        } else {
            return s;
        }
    }

	/**
	 * Converts a byte to an unsigned value.
	 */
	public static int toUnsigned(final byte b) {
		return b & 0xFF;
	}
	/**
	 * Converts a short to an unsigned value.
	 */
	public static int toUnsigned(final short s) {
		return s & 0xFFFF;
	}
	/**
	 * Converts an int to an unsigned value.
	 */
	public static long toUnsigned(final int i) {
		return i & 0xFFFFFFFFL;
	}
    
	/**
	 * Gets the hexadecimal representation of the given number. The result is 
	 * prefixed with '0' until the given length is reached.
	 * @param number
	 * @param length
	 * @return String
	 */
	public static String hex(int number, int length) {
		StringBuilder buf = new StringBuilder(length);
		int2HexString(buf, number);
		return prefixZero(buf, length);
	}
	
	/**
	 * Gets the hexadecimal representation of the given number that is
	 * 8 digits long.
	 * @param number
	 * @return String
	 */
	public static String hex(int number) {
		return hex(number, 8);
	}
	
	/**
	 * Gets the hexadecimal representation of the given number. The result is 
	 * prefixed with '0' until the given length is reached.
	 * @param number
	 * @param length
	 * @return String
	 */
	public static String hex(long number, int length) {
		StringBuilder buf = new StringBuilder(length);
		long2HexString(buf, number);
		return prefixZero(buf, length);
	}
	
	/**
	 * Gets the hexadecimal representation of the given number that is
	 * 16 digits long.
	 * @param number
	 * @return String
	 */
	public static String hex(long number) {
		return hex(number, 16);
	}
	
	/**
	 * Convert a byte array to a string of hex-numbers
	 * @param data
	 * @param offset
	 * @param length
	 * @return String
	 */
	public static String hex(byte[] data, int offset, int length) {
		final StringBuilder buf = new StringBuilder(length*3);
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				if ((i % 16) == 0) {
					buf.append('\n');
				} else {
					buf.append(' ');
				}
			}
			buf.append(hex(data[offset+i] & 0xFF, 2));
		}
		return buf.toString();
	}
	
	/**
	 * Convert a int array to a string of hex-numbers
	 * @param data
	 * @param offset
	 * @param length
	 * @param hexLength
	 * @return String
	 */
	public static String hex(int[] data, int offset, int length, int hexLength) {
		final StringBuilder buf = new StringBuilder(length*(hexLength+1));
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				if ((i % 16) == 0) {
					buf.append('\n');
				} else {
					buf.append(' ');
				}
			}
			buf.append(hex(data[offset+i], hexLength));
		}
		return buf.toString();
	}

    /**
     * Convert a char array to a string of hex-numbers
     * @param data
     * @param offset
     * @param length
	 * @return String
     */
    public static String hex(char[] data, int offset, int length) {
        final StringBuilder buf = new StringBuilder(length*3);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                if ((i % 16) == 0) {
                    buf.append('\n');
                } else {
                    buf.append(' ');
                }
            }
            buf.append(hex(data[offset+i], 2));
        }
        return buf.toString();
    }


	/**
	 * Convert a byte array to a string of hex-numbers
	 * @param data
	 * @return String
	 */
	public static String hex(byte[] data) {
		return hex(data, 0, data.length);
	}
	
	/**
	 * Convert an int array to a string of hex-numbers
	 * @param data
	 * @param hexLength
	 * @return String
	 */
	public static String hex(int[] data, int hexLength) {
		return hex(data, 0, data.length, hexLength);
	}
	
	private static String prefixZero(StringBuilder v, int length) {
		if (v.length() > length) {
            // truncate leading chars
			return v.substring(v.length() - length);
		} else {
            // insert leading '0's
			while (v.length() < length) {
				v.insert(0, '0');
			}
			return v.toString();
		}
	}
	
	/** 
	 * Convert the given value to a size string like 64K
	 * @param v the size to convert
	 * @return the text for of the size
	 */
	public static String size(long v) {
	    // Is < 1Kb?
	    if (v < K) {
	        return String.valueOf(v) + "B";
	    }
	    // Is < 1Mb?
	    v = v >>> 10;
	    if (v < K) {
	        return String.valueOf(v) + "KB";
	    }
	    // Is < 1Gb?
	    v = v >>> 10;
	    if (v < K) {
	        return String.valueOf(v) + "MB";
	    }
	    // Is < 1Tb?
	    v = v >>> 10;
	    if (v < K) {
	        return String.valueOf(v) + "GB";
	    }
	    // Large...
	    v = v >>> 10;
    	    return String.valueOf(v) + "TB";
	}
    
    /**
     * 
     * @param size a number eventually followed by  a multiplier (K: Kilobytes, M: Megabytes, G:Gigabytes)  
     * @return the numeric value of the size
     */
    public static long getSize(String size) {
        if((size == null) || size.trim().equals(""))
            return 0;
        
        long multiplier = 1;

        if(size.endsWith("B"))
            size = size.substring(0, size.length() - 1);

        if(size.endsWith("T")) {
            multiplier = T;
            size = size.substring(0, size.length() - 1);
        } else if(size.endsWith("G")) {
            multiplier = G;
            size = size.substring(0, size.length() - 1);
        } else if(size.endsWith("M")) {
            multiplier = M; 
            size = size.substring(0, size.length() - 1);
        } else if(size.endsWith("K")) {
            multiplier = K;          
            size = size.substring(0, size.length() - 1);
        }
    
        return Long.parseLong(size) * multiplier;
    }
        	
	/**
	 * This method avoids the use on Integer.toHexString, since this class may be used 
	 * during the boot-phase when the Integer class in not yet initialized.
	 * @param buf
	 * @param value
	 */
	private static void int2HexString(StringBuilder buf, int value) {
		int rem = value & 0x0F;
		int q = value >>> 4;
		if (q != 0) {
			int2HexString(buf, q);
		}
		
		if (rem < 10) {
			buf.append((char)('0' + rem));
		} else {
			buf.append((char)('A' + rem - 10));
		}
	}
	
	/**
	 * This method avoids the use on Long.toHexString, since this class may be used 
	 * during the boot-phase when the Long class in not yet initialized.
	 * @param buf
	 * @param value
	 */
	private static void long2HexString(StringBuilder buf, long value) {
//		long rem = value & 0x0F;
		int rem = (int)(value & 0x0FL);
		long q = value >>> 4;
		if (q != 0) {
			long2HexString(buf, q);
		}
		
		if (rem < 10) {
			buf.append((char)('0' + rem));
		} else {
			buf.append((char)('A' + rem - 10));
		}
	}
}
