/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CastTest {

	public static void main(String[] args) {
		test(args);
		test2("Hello world");
	}

	public static void test(Object args) {
		System.out.println("args.class=" + args.getClass().getName());
		Object[] arr = (Object[]) args;
		System.out.println(arr);
		
		if (args instanceof String[]) {
			System.out.println("Instanceof");
		} else {
			System.out.println("Not instanceof: " + args.getClass().getName());
		}
	}
	
	public static void test2(Object arg) {
		try {
			((Integer)arg).intValue();
		} catch (ClassCastException ex) {
			ex.printStackTrace();
		}
	}
}
