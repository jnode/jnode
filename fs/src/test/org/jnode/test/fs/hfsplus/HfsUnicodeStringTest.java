/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

}
