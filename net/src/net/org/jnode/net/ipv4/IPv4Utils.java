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
