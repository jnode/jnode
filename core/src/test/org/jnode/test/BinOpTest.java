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
		
		System.out.println("i1 % i2=" + NumberUtils.hex(i1 % i2));
		System.out.println("i1 & i2=" + NumberUtils.hex(i1 & i2));
		System.out.println("i1 | i2=" + NumberUtils.hex(i1 | i2));
		System.out.println("i1 ^ i2=" + NumberUtils.hex(i1 ^ i2));
		
	}
}
