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
 
package org.jnode.test.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public class ProxyTest {

    /**
     * @param args
     * @throws java.io.FileNotFoundException
     * @throws
     */
    public static void main(String[] args) throws IOException {

        IFoo origFoo = new FooImpl();

        final ClassLoader loader = ProxyTest.class.getClassLoader();
        IFoo proxy = (IFoo) Proxy.newProxyInstance(loader,
            new Class[]{IFoo.class}, new Handler<IFoo>(origFoo));

        proxy.foo();

        if (true) {
            byte[] classData = ProxyBuilder.getProxyClass(loader,
                new Class[]{IFoo.class});
            FileOutputStream os = new FileOutputStream("$Proxy0.class");
            os.write(classData);
            os.close();
        }
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    public static interface IFoo {
        public void foo();

        public int intFoo()
            throws IOException;
    }

    public static class FooImpl implements IFoo {

        /**
         * @see org.jnode.test.core.ProxyTest.IFoo#foo()
         */
        public void foo() {
            System.out.println("Foo");
        }

        public int intFoo()
            throws IOException {
            return 5;
        }

    }

    public static class Handler<T> implements InvocationHandler {

        private final T object;

        public Handler(T object) {
            this.object = object;
        }

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            return method.invoke(object, args);
        }
    }

    public static class MyProxy {
        private static Method[] m;
        private InvocationHandler h;

        public void foo() {
            try {
                h.invoke(this, m[2], null);
            } catch (Error e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }

    }
}
