/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class CurrentTimeMillisTest {

	public static void main(String[] args) {
		
		System.out.println("Testing System.currentTimeMillis please wait");
		
		for (int i = 0; i < 1000; i++) {
			final long start = System.currentTimeMillis();
			
			final long end = System.currentTimeMillis();
			
			int count = 0;
			for (int j = 0; j < 50000; j++) {
				count += j;
			}
			
			final long diff = end-start;
			
			if (diff < 0) {
				System.out.println("Oops currentTimeMillis goes back in time " + diff + "ms");
			}
			
		}
		
		System.out.println("done");
	}
}
