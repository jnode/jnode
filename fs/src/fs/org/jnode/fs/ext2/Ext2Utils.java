/*
 * $Id$
 */
package org.jnode.fs.ext2;

/**
 * @author epr
 * (every method copied from DosUtils)
 */
public class Ext2Utils {
	/**
	 * Gets an unsigned 8-bit byte from a given offset
	 * @param offset
	 * @return int
	 */
	public static short get8(byte[] data, int offset) {
		return (short) (data[offset] & 0xFF);
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
	public static int get16(byte[] data, int offset) {
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
	 * Can't read from blocks bigger in size than 2GB (32bit signed int)
	 * 
	 * @param offset
	 * @return long
	 */	
	public static long get32(byte[] data, int offset) {
		int b1 = data[offset] & 0xFF;
		int b2 = data[offset+1] & 0xFF;
		int b3 = data[offset+2] & 0xFF;
		int b4 = data[offset+3] & 0xFF;
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
		data[offset+3] = (byte)((value >> 32) & 0xFF);
	}

}
