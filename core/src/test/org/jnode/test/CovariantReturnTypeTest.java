/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CovariantReturnTypeTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final A a = new A();
        final B b = new B();
        final A ab = new B();
        
        System.out.println("a.foo  = " + a.foo());
        System.out.println("b.foo  = " + b.foo());
        System.out.println("ab.foo = " + ab.foo());
    }

    public static class A {
        public Object foo() {
            return null;
        }
    }
    
    public static class B extends A {
        public String foo() {
            return "Hello world";
        }
    }
}
