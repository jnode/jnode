/*
 * $Id$
 */
package org.jnode.net.ipv4;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class IPv4Utils {

	/**
	 * Calculate the checksum of the given header
	 * @param skbuf
	 * @param start
	 * @param length
	 * @return The calculated checksum
	 */
	public static int calcChecksum(SocketBuffer skbuf, int start, int length) {
		return calcChecksum(skbuf, start, length, ~0);
	}
	
	/**
	 * Calculate the checksum of the given header
	 * @param skbuf
	 * @param start
	 * @param length
	 * @param initialValue Result from a previous call to calcChecksum. Use when to blocks are concatenated
	 * @return The calculated checksum
	 */
	public static int calcChecksum(SocketBuffer skbuf, int start, int length, int initialValue) {
		final int size = skbuf.getSize();
		int chsum = ~initialValue;
		for (int i = 0; i < length; i += 2) {
			final int v;
			if (i+1 >= size) {
				v = (skbuf.get(start+i) << 8);
			} else {
				v = skbuf.get16(start+i);
			}
			chsum += v;
			if ((chsum & 0xffff0000) != 0) {
				chsum++;
				chsum &= 0xffff;
			}
		}
		/*while ((chsum >> 16) != 0) {
			chsum = (chsum & 0xffff) + (chsum >> 16);
		}*/

		//return (short)(~sum);
		return (short)(~chsum);
	}
}
