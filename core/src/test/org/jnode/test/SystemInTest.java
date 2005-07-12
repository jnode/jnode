/*
 * $Id$
 */
package org.jnode.test;

import java.io.IOException;

public class SystemInTest {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        System.out.println("System.in: " + System.in.getClass().getName());
        System.out.println("Type letters ending with A");
        int ch;
        while ((ch = System.in.read()) != 'A') {
            System.out.print((char)ch);
        }
        System.out.println();
    }

}
