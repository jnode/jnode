/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DoubleTest {

	public static void main(String[] args) {
		d2i(0.0);
		d2i(0.4);
		d2i(0.5);
		d2i(0.6);
		d2i(1.0);
		d2i(1.1);
		d2i(1.4);
		d2i(1.5);
		d2i(1.6);
		d2i(1.9);
		d2i(2.0);
	}

	public static void d2i(double d) {
		final int i = (int) d;
		System.out.println("d=" + d + ", i=" + i);
	}
}
