/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.test;

/**
 * @author epr
 */
public class ArrayTest {

    public static int simpleByte(byte[] arr, int i) {
        arr[i + 4] = arr[i];
        return arr[i];
    }

    public static char simpleChar(char[] arr, int i) {
        arr[i + 4] = arr[i];
        return arr[i];
    }

    public static int simpleInt(int[] arr, int i) {
        arr[i + 4] = arr[i];
        return arr[i];
    }

    public static int mulTest(int a, int b) {
        return a * b;
    }

    public static int shlTest(int a, int b) {
        return a << b;
    }

    public static int shrTest(int a, int b) {
        return a >> b;
    }

    public static int sarTest(int a, int b) {
        return a >>> b;
    }

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
            for (int i = 0; i < 100000; i++) {
                arr[i % 27] = arr[i % 13];
            }
            final long end = System.currentTimeMillis();
            System.out.println("Test succeeded in " + (end - start) + "ms");
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
