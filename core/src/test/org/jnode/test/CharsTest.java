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

/**
 * Print all (8-bit) chars to the console, for debugging purposes.
 * This demonstrates the support for 8-bit chars introduced to
 * the output driver.
 *
 * @author Bengt B\u00e4verman
 * @since 2003-08
 */
public class CharsTest {
    public static void main(String[] args) {
        System.out.println("Test written by Bengt B\u00e4verman");

        System.out.println(
            "Some chars used in Sweden: \u00e5\u00e4\u00f6 \u00c5\u00c4\u00d6 \u00c9\u00e9\u00fc\u00dc \u00e1\u00e0");

        System.out.println(
            "Other chars: \u00bf \u00c7\u00e7 \u00c9\u00e9 \u00e8\u00eb \u00dc\u00fc\u00f9\u00fa " +
                "\u00e0\u00e1\u00f2\u00f3 \u00d1\u00f1 \u00a3$\u00a5 \u00ce\u00e2\u00ea\u00f4\u00fb ");

        System.out.println();
        System.out.print("All 8-bit chars in groups of 64:");
        for (int i = 0; i < 256; i++) {
            if (i % 64 == 0) System.out.println();

            System.out.print((char) i);
        }
        System.out.println();
    }
}
