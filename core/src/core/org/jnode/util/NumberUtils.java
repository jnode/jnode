/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class NumberUtils {
	
    public static final int K = 1024;
    public static final int M = 1024*1024;
    public static final int G = 1024*1024*1024;
    
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
	 * Gets the hexadecimal representation of the given number. The result is 
	 * prefixed with '0' until the given length is reached.
	 * @param number
	 * @param length
	 * @return String
	 */
	public static String hex(int number, int length) {
		StringBuffer buf = new StringBuffer();
		int2HexString(buf, number);
		return prefixZero(buf.toString(), length);
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
		StringBuffer buf = new StringBuffer();
		long2HexString(buf, number);
		return prefixZero(buf.toString(), length);
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
		final StringBuffer buf = new StringBuffer(length*3);
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
		final StringBuffer buf = new StringBuffer(length*(hexLength+1));
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
        final StringBuffer buf = new StringBuffer(length*3);
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
	
	public static String prefixZero(String v, int length) {
		if (v.length() > length) {
			return v.substring(v.length() - length);
		} else {
			while (v.length() < length) {
				v = "0" + v;
			}
			return v;
		}
	}
	
	/** 
	 * Convert the given value to a size string like 64K
	 * @param v
	 * @return
	 */
	public static String size(long v) {
	    // Is < 1Kb?
	    if ((v & (K-1)) != 0) {
	        return String.valueOf(v) + "b";
	    }
	    // Is < 1Mb?
	    v = v >>> 10;
	    if ((v & (K-1)) != 0) {
	        return String.valueOf(v) + "K";
	    }
	    // Is < 1Gb?
	    v = v >>> 10;
	    if ((v & (K-1)) != 0) {
	        return String.valueOf(v) + "M";
	    }
	    // Large...
	    v = v >>> 10;
        return String.valueOf(v) + "G";
	}
	
	/**
	 * This method avoids the use on Integer.toHexString, since this class may be used 
	 * during the boot-fase when the Integer class in not yet initialized.
	 * @param buf
	 * @param value
	 */
	private static void int2HexString(StringBuffer buf, int value) {	
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
	 * during the boot-fase when the Long class in not yet initialized.
	 * @param buf
	 * @param value
	 */
	private static void long2HexString(StringBuffer buf, long value) {	
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
