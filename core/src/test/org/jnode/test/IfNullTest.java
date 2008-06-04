/*
 * $Id$
 */
package org.jnode.test;

public class IfNullTest {

    static final Object nullVariable = null;
    static final Object nonNullVariable = new Object();

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestNullVariable();
        TestNonNullVariable();
    }

    static void TestNullVariable() {
        if (nullVariable == null) {
            System.out.println("nullVariable == null");
        } else {
            System.out.println("nullVariable != null");
        }
    }

    static void TestNonNullVariable() {
        if (nonNullVariable == null) {
            System.out.println("nonNullVariable == null");
        } else {
            System.out.println("nonNullVariable != null");
        }
    }

}
