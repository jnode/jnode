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

import java.lang.annotation.Annotation;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AnnotationTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        show("Annotations for class A", A.class.getAnnotations());
        show("Annotations for class B", B.class.getAnnotations());
//        show("Annotations for class Test", Test.class.getAnnotations());
//        show("Annotations for class Test2", Test2.class.getAnnotations());
//        show("Annotations for class Test3", Test3.class.getAnnotations());
//        show("Annotations for class Test4", Test4.class.getAnnotations());
//        show("Declared annotations for class A", A.class.getDeclaredAnnotations());
//        show("Declared annotations for class B", B.class.getDeclaredAnnotations());
    }

    private static void show(String msg, Annotation[] ann) {
        System.out.println(msg);
        for (Annotation a : ann) {
            System.out.println(a);
        }
        System.out.println();
    }

    @Test
    @Test4
    public static class A {

    }

    @Test2(name = "ewout", descr = "programmer")
    @Test3(name = 5, descr = {1, 2, 3, 4 })
    public static class B extends A {

        @Test
        public void foo() {

        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({TYPE, METHOD })
    public @interface Test {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    public @interface Test2 {
        String name();

        String descr();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    public @interface Test3 {
        int name();

        int[] descr();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({TYPE, METHOD })
    @Inherited
    public @interface Test4 {
    }


}
