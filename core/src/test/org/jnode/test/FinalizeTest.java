/*
 * $Id$
 */
package org.jnode.test;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FinalizeTest {

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new FinalizeTest(i);
        }
        Runtime.getRuntime().gc();
        System.out.println("Done");
    }
    
    private final int i;
    
    public FinalizeTest(int i) {
        this.i = i;
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    public void finalize() {
        System.out.println("Finalize called on " + i);
    }
    
}
