/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeStaticTest {

	public static void main(String[] args) {
		final long start = System.currentTimeMillis();
		int v = 0;
		for (int i = 0; i < 100000; i++) {
			v = foo(v);
		}
		final long end = System.currentTimeMillis();
		System.out.println("Test returned " + v + " in " + (end - start) + "ms");

	}

	public static int foo(int v) {
		return v + 1;
	}
}
