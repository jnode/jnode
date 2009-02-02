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

import org.jnode.util.StopWatch;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeInterfaceTest {

    public static void main(String[] args) {
        A a = new B();

        final StopWatch sw = new StopWatch();
        int a1 = 1;
        int a2 = 1;
        for (int i = 0; i < 10000; i++) {
            a1 = a.foo(a1);
            a2 = a.foo2(a1, a2);
        }
        System.out.println("a1=" + a1 + ", a2=" + a2);
        System.out.println("Time taken " + sw);
    }

    static interface A {
        public int foo(int a1);

        public int foo2(int a1, int a2);
    }

    static class B implements A {
        public int foo2(int a1, int a2) {
            return a1 + a2 + a1;
        }

        public int foo(int a1) {
            return a1 + a1;
        }
    }
}
