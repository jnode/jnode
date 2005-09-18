/*
 * $Id$
 */
package org.jnode.test;

public class InstanceofTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Object o = new InstanceofTest();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    for (int l = 0; l < 10; l++) {
                        System.out.println("ok=" + (o instanceof InstanceofTest));
                    }
                }
            }
        }
        // TODO Auto-generated method stub

    }
    
    

}
