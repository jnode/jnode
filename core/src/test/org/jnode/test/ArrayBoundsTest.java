/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ArrayBoundsTest {

	public static void main(String[] args) {
		test(args, -1, false);
		test(args, args.length, false);
		if (args.length > 0) {
			test(args, 0, true);
			test(args, args.length-1, true);
		}
	}
	
	private static void test(String[] arr, int index, boolean ok) {
		try {
			System.out.println(arr[index]);
			if (!ok) {
				throw new RuntimeException("Test should fail at index " + index);
			} else {
				System.out.println("Ok");
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			if (ok) {
				throw ex;
			} else {
				System.out.println("Ok: " + ex.getMessage());
			}
		}
	}
	
}
