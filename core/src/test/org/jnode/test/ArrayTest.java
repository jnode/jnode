/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class ArrayTest {

	public static void main(String[] args) {

		/*
		 * int[][] array; array = new int[2][3];
		 * 
		 * for (int a = 0; a < 2; a++) { for (int b = 0; b < 3; b++) { array[a][b] = a + b; } }
		 * 
		 * for (int a = 0; a < 2; a++) { System.out.println("A: " + a); for (int b = 0; b
		 * < 3; b++) { System.out.println("B: " + b + " -> " + array[a][b]); }
		 */

		final int[] arr = new int[27];
		boolean ok = true;
		ok &= test(arr, 0, true);
		ok &= test(arr, 26, true);
		ok &= test(arr, 13, true);
		ok &= test(arr, -1, false);
		ok &= test(arr, 27, false);
		ok &= test(arr, Integer.MAX_VALUE, false);
		ok &= test(arr, Integer.MIN_VALUE, false);
		
		if (ok) {
			final long start = System.currentTimeMillis();
			for (int i = 0;i < 100000; i++) {
				arr[i % 27] = arr[i % 13];
			}
			final long end = System.currentTimeMillis();
			System.out.println("Test succeeded in " + (end-start) + "ms");
		}
		
	}

	static boolean test(int[] arr, int index, boolean mustSucceed) {
		try {
			arr[index] = index;
			if (!mustSucceed) {
				System.out.println("Test arr[" + index + "] failed");
				return false;
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			if (mustSucceed) {
				System.out.println("Test arr[" + index + "] failed");
				return false;
			}
		}
		return true;
	}
}
