/*
 * $Id$
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
	}
}
