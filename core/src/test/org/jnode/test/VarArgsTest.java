/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VarArgsTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        test("Hello", "World");
        test("Hello", "Wide", "World");
        test("Hello", "World", 1982);
    }

    private static void test(String msg, Object... args) {
        System.out.println("msg=" + msg);
        System.out.println("#args=" + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]=" + args[i] + ", " + args[i].getClass().getName());            
        }
    }
}
