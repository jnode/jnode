/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
