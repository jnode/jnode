/*
 * $Id$
 */
package org.jnode.test;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LongTest {

    public static void main(String[] args) {
        new LongTest().run();
    }
    
    public void run() {
        long a[] = new long[0x9743];
        a[0] = allocNew();
        System.out.println(a[1]);
        System.out.println(a[0]);
    }
    
    public long allocNew() {
        int i = 5;
        i++;
        return i;
    }
}
