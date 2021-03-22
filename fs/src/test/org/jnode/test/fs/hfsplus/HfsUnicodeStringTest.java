/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.test.fs.hfsplus;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HfsUnicodeStringTest {
    private byte[] STRING_AS_BYTES_ARRAY =
        new byte[]{0, 8, 0, 116, 0, 101, 0, 115, 0, 116, 0, 46, 0, 116, 0, 120, 0, 116};
    private String STRING_AS_TEXT = "test.txt";

    @Test
    public void testConstructAsBytesArray() {
        HfsUnicodeString string = new HfsUnicodeString(STRING_AS_BYTES_ARRAY, 0);
        assertEquals(8, string.getLength());
        assertEquals(STRING_AS_TEXT, string.getUnicodeString());
    }

    @Test
    public void testConstructAsString() {
        HfsUnicodeString string = new HfsUnicodeString(STRING_AS_TEXT);
        assertEquals(8, string.getLength());
        byte[] array = string.getBytes();
        int index = 0;
        for (byte b : array) {
            assertEquals(STRING_AS_BYTES_ARRAY[index], b);
            index++;
        }
    }

    @Test
    public void testEquals() {
        HfsUnicodeString string1 = new HfsUnicodeString(STRING_AS_TEXT);
        HfsUnicodeString string2 = new HfsUnicodeString(STRING_AS_TEXT);
        HfsUnicodeString string3 = new HfsUnicodeString(null);
        HfsUnicodeString string4 = new HfsUnicodeString(null);

        assertEquals(string1, string2);
        assertEquals(string3, string4);
        assertFalse(string1.equals(string3));
        assertFalse(string4.equals(string2));
    }

    @Test
    public void testCompareTo() {
        HfsUnicodeString nullStr = new HfsUnicodeString(null);
        HfsUnicodeString emptyStr = new HfsUnicodeString("");
        HfsUnicodeString string1 = new HfsUnicodeString("test");
        HfsUnicodeString string2 = new HfsUnicodeString("test");
        HfsUnicodeString longerStr = new HfsUnicodeString("testzzz");

        assertEquals(-1, nullStr.compareTo(emptyStr));
        assertEquals(1, emptyStr.compareTo(nullStr));

        assertEquals(0, string1.compareTo(string2));
        assertTrue(string1.compareTo(longerStr) < 0);
        assertTrue(longerStr.compareTo(string1) > 0);

        assertEquals(1, string1.compareTo(nullStr));
        assertEquals(-1, nullStr.compareTo(string1));
    }
}
