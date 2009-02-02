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
 
package org.jnode.test.core;

import java.lang.reflect.Method;

/**
 * This is the testcase for issue 888.  java.reflect.Method did not check that
 * that the target object was correct.
 *
 * @author Peter Barth
 */
public class ReflectiveInvokeTest {

    static class A {
        private String foo = "Very secret message!!";

        public A() {
        }
    }

    static class B {
        public String foo = "Not so secret message";

        public String getFoo() {
            return foo;
        }
    }

    public static void main(String[] args) {

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {

            Class<?> cls_a = cl.loadClass("org.jnode.test.core.ReflectiveInvokeTest$A");
            Class<?> cls_b = cl.loadClass("org.jnode.test.core.ReflectiveInvokeTest$B");

            Object a = cls_a.newInstance();

            Method method = cls_b.getMethod("getFoo", new Class[]{});
            Object result = method.invoke(a, new Object[]{});

            System.out.println("Result = " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
