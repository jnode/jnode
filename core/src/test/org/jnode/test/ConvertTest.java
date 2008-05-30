/*
 * $Id$
 */
package org.jnode.test;

public class ConvertTest {

    public static void main(String[] argv) {
        System.out.println(test1());
        System.out.println(test2());
    }

    public static boolean test1() {
        double a = 0.5;
        long b = (long) a;
        return b == a;
    }

    public static boolean test2() {
        double a = 0.5;
        return (long) a == a;
    }
}
