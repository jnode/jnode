/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class IntTest {

	public static void main(String[] args) {
		new IntTest().run();
	}

	public IntTest() {

	}

	public void run() {
		test(0, 5);
		test(-1481821192, 1);
	}

	public void test(int a, int b) {
		System.out.println("a     = " + a);
		System.out.println("b     = " + b);
		System.out.println("a % b = " + (a % b));
	}
}
