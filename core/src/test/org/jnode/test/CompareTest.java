/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class CompareTest {

	public static void main(String[] args) {
		
		final int i = 0x80000000;
		
		if (i < 0) {
			System.out.println("i < 0");
		} else {
			System.out.println("i >= 0");
		}
		
	}
}
