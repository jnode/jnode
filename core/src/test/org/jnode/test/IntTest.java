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
		testInt(0, 5);
		testInt(-1481821192, 1);
		testLong(0L, 5L);
		testLong(-1481821192L, 1L);
	}

	public void testInt(int a, int b) {
		System.out.println("int");
		System.out.println("a     = " + a);
		System.out.println("b     = " + b);
		System.out.println("-a    = " + (-a));
		System.out.println("-b    = " + (-b));
		System.out.println("a + b = " + (a + b));
		System.out.println("a - b = " + (a - b));
		System.out.println("a * b = " + (a * b));
		System.out.println("a / b = " + (a / b));
		System.out.println("a % b = " + (a % b));
	}

	public void testLong(long a, long b) {
		System.out.println("long");
		System.out.println("a     = " + a);
		System.out.println("b     = " + b);
		System.out.println("-a    = " + (-a));
		System.out.println("-b    = " + (-b));
		System.out.println("a + b = " + (a + b));
		System.out.println("a - b = " + (a - b));
		System.out.println("a * b = " + (a * b));
		System.out.println("a / b = " + (a / b));
		System.out.println("a % b = " + (a % b));
	}
}
