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
