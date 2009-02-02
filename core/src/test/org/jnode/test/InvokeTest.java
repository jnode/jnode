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
 * Test for virtual method invocation.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeTest {

    public static void main(String[] args) {
        A a1 = new A();
        A a2 = new B();

        System.out.print("a1.foo  : (expect A.foo) =");
        a1.foo();
        System.out.print("a1.foo2 : (expect A.foo2)=");
        a1.foo2();

        System.out.print("a2.foo  : (expect B.foo) =");
        a2.foo();
        System.out.print("a2.foo2 : (expect B.foo2)=");
        a2.foo2();
    }

    static class A {
        public void foo() {
            System.out.println("A.foo");
        }

        public void foo2() {
            System.out.println("A.foo2");
        }
    }

    static class B extends A {
        public void foo2() {
            System.out.println("B.foo2");
        }

        public void foo() {
            System.out.println("B.foo");
        }
    }
}
