/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ArrayLongTest {

    public static void main(String[] args) {
        for (long i = 0; i < 128; i++) {
            new ArrayLongTest().test(i, 63);
        }
    }
    
    private final Object[] arr = new Object[128];
    
    public void test(long l1, long l2) {
        int group = (int)(l1 / l2);
        int index = (int)(l1 % l2);
        Object obj = arr[group];
    }
}
