/*
 * $Id$
 */
package org.jnode.test;

import java.util.ArrayList;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ForEachTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        testArray();
        testCollection();
    }

    private static void testArray() {
        final int[] a = new int[] { 1, 2, 3, 4, 5 };
        
        for (int i : a) {
            System.out.println("i=" + i);
        }
    }
    
    private static void testCollection() {
        final ArrayList<String> list = new ArrayList<String>();
        list.add("Aap");
        list.add("Noot");
        list.add("Mies");
        
        for (String i : list) {
            System.out.println("i=" + i);
        }
    }   
}
