/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HoldsLockTest {

	public static void main(String[] args) {
		
		final Object o = new Object();
		
		test(o, false);
		synchronized (o) {
			test(o, true);
		}
		test(o, false);
	}
	
	private static void test(Object obj, boolean expectedResult) {
		final boolean result = Thread.holdsLock(obj);
		if (result != expectedResult) {
			System.out.println("Test failed: holdsLock=" + result + ", but " + expectedResult + " was expected");
		} else {
			System.out.println("Ok");
		}
	}
}
