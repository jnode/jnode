/*
 * $Id$
 */
package org.jnode.test;

import java.util.ArrayList;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InlineTestClass {

	static Object tester;
	private int x;
	
	static {
		tester = new ArrayList();
	}

	public void foo2() {
		int i = 5;
		int j = -7;
		x = i+j;
	}
	
	/*public void foo() {
		while (getX() < 5) {
			System.out.println(getX());
			x = getX() + 1;
		} 
	}*/

	/**
	 * @return The X value
	 */
	public final int getX() {
		return this.x;
	}
	
	public static final int dummy() {
		return 15;
	}
}
