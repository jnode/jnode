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
 
package org.jnode.test;

import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class BinOpTest {

	public static void main(String[] args) {
		
		final int i1 = 0x00450078;
		final int i2 = 0x45007800;
		final long l1 = 0xAABB220000450078L;
		final long l2 = 0x0022BBAA45007800L;
		
		System.out.println("i1 % i2  =" + NumberUtils.hex(i1 % i2));
		System.out.println("i1 & i2  =" + NumberUtils.hex(i1 & i2));
		System.out.println("i1 | i2  =" + NumberUtils.hex(i1 | i2));
		System.out.println("i1 ^ i2  =" + NumberUtils.hex(i1 ^ i2));
		
		System.out.println("l1 % l2  =" + NumberUtils.hex(l1 % l2));
		System.out.println("l1 & l2  =" + NumberUtils.hex(l1 & l2));
		System.out.println("l1 | l2  =" + NumberUtils.hex(l1 | l2));
		System.out.println("l1 ^ l2  =" + NumberUtils.hex(l1 ^ l2));
		
		System.out.println("l1 >> 3  =" + NumberUtils.hex(l1 >> 3));
		System.out.println("l1 >> 33 =" + NumberUtils.hex(l1 >> 33));
		System.out.println("l1 >>> 3 =" + NumberUtils.hex(l1 >>> 3));
		System.out.println("l1 >>> 33=" + NumberUtils.hex(l1 >>> 33));

		System.out.println("i1 & 0xFF         = " + (byte) (i1 & 0xFF));
		System.out.println("(i1 >> 8) & 0xFF  = " + (byte) ((i1 >> 8) & 0xFF));
		System.out.println("(i1 >> 16) & 0xFF = " + (byte) ((i1 >> 16) & 0xFF));
		System.out.println("(i1 >> 24) & 0xFF = " + (byte) ((i1 >> 24) & 0xFF));

		System.out.println("i2 & 0xFF         = " + (byte) (i2 & 0xFF));
		System.out.println("(i2 >> 8) & 0xFF  = " + (byte) ((i2 >> 8) & 0xFF));
		System.out.println("(i2 >> 16) & 0xFF = " + (byte) ((i2 >> 16) & 0xFF));
		System.out.println("(i2 >> 24) & 0xFF = " + (byte) ((i2 >> 24) & 0xFF));
	}
}
