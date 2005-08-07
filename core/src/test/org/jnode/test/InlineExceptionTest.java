/*
 * $Id$
 */
package org.jnode.test;

public class InlineExceptionTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            f1();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void f1() {
        f2();   
    }
    
    public static void f2() {
        f3();
    }
    
    public static void f3() {
        throw new RuntimeException();
    }
}
