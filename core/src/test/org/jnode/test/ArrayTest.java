/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class ArrayTest {

	public static void main(String[] args) {
		
		/*int[][] array;
		array = new int[2][3];
		
		for (int a = 0; a < 2; a++) {
			for (int b = 0; b < 3; b++) {
				array[a][b] = a + b;
			}
		}
		
		for (int a = 0; a < 2; a++) {
			System.out.println("A: " + a);
			for (int b = 0; b < 3; b++) {
				System.out.println("B: " + b + " -> " + array[a][b]);
			}
		}*/
		
		final int[] arr = new int[27];
		test(arr, 0, true);
		test(arr, 26, true);
		test(arr, 13, true);
		test(arr, -1, false);
		test(arr, 27, false);
		test(arr, Integer.MAX_VALUE, false);
		test(arr, Integer.MIN_VALUE, false);
		
		
	}
	
	static void test(int[] arr, int index, boolean mustSucceed) {
		try {
			arr[index] = index;
			if (!mustSucceed) {
				System.out.println("Test arr[" + index + "] failed");
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			if (mustSucceed) {
				System.out.println("Test arr[" + index + "] failed");
			}
		}
	}
}
