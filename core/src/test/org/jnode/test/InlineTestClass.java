/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InlineTestClass {

	private static int x;

	public void foo2() {
		int i = 5;
		int j = -7;
		x = i + j;
	}

	/*
	 * public void foo() { while (getX() < 5) { System.out.println(getX()); x = getX() + 1; }
	 */

	/**
	 * @return The X value
	 */
	public final int getX() {
		return x;
	}
}
