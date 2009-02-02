/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test;

import java.util.ArrayList;
import junit.framework.TestCase;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class ForEachTest extends TestCase {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ForEachTest test = new ForEachTest();
        test.testIntArray();
        test.testStringArray();
        test.testCollection();
    }

    public void testIntArray() {
        final int[] a = new int[]{0, 1, 2, 3, 4, 5};

        int expected = 0;
        for (int i : a) {
            assertEquals(expected, i);
            expected++;
        }
        assertEquals("array not fully iterated", expected, a.length);
    }

    public void testStringArray() {
        final String[] a = new String[]{"A", "B", "C", "D", "E"};

        int idxExpected = 0;
        for (String s : a) {
            assertEquals(a[idxExpected], s);
            idxExpected++;
        }
        assertEquals("array not fully iterated", idxExpected, a.length);
    }

    public void testCollection() {
        final ArrayList<String> list = new ArrayList<String>();
        list.add("Aap");
        list.add("Noot");
        list.add("Mies");

        int idxExpected = 0;
        for (String s : list) {
            assertEquals(list.get(idxExpected), s);
            idxExpected++;
        }
        assertEquals("collection not fully iterated", idxExpected, list.size());
    }
}
