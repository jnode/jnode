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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.test;

/**
 * @author epr
 */
public class IntTest {

    public static void main(String[] args) {
        new IntTest().run();
    }

    public IntTest() {

    }

    public void run() {
        testInt(0, 5);
        testInt(-1481821192, 1);
        testLong(0L, 5L);
        testLong(-1481821192L, 1L);
    }

    public void testInt(int a, int b) {
        System.out.println("int");
        System.out.println("a     = " + a);
        System.out.println("b     = " + b);
        System.out.println("-a    = " + (-a));
        System.out.println("-b    = " + (-b));
        System.out.println("a + b = " + (a + b));
        System.out.println("a - b = " + (a - b));
        System.out.println("a * b = " + (a * b));
        System.out.println("a / b = " + (a / b));
        System.out.println("a % b = " + (a % b));
    }

    public void testLong(long a, long b) {
        System.out.println("long");
        System.out.println("a     = " + a);
        System.out.println("b     = " + b);
        System.out.println("-a    = " + (-a));
        System.out.println("-b    = " + (-b));
        System.out.println("a + b = " + (a + b));
        System.out.println("a - b = " + (a - b));
        System.out.println("a * b = " + (a * b));
        System.out.println("a / b = " + (a / b));
        System.out.println("a % b = " + (a % b));
    }
}
