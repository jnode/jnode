/*
 * $Id$
 *
 * JNode.org
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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CovariantReturnTypeTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final A a = new A();
        final B b = new B();
        final A ab = new B();

        System.out.println("a.foo  = " + a.foo());
        System.out.println("b.foo  = " + b.foo());
        System.out.println("ab.foo = " + ab.foo());

        final String s = b.foo();

        System.out.println("s = " + s);
    }

    public static class A {
        public Object foo() {
            return null;
        }
    }

    public static class B extends A {
        public String foo() {
            return "Hello world";
        }
    }
}
