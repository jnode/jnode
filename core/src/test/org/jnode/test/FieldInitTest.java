/*
 * $Id$
 */
package org.jnode.test;

public class FieldInitTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new FieldInitTest().DoSomething();
    }
    
    StringBuilder sb = new StringBuilder();
    
    public void DoSomething() {
        sb.append("Hello world");
    }

}
