/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class ArrayTest {

	public static void main(String[] args) {
		
		int[][] array;
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
		}
		
		
	}
}
