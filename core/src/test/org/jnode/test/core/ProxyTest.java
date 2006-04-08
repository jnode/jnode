/*
 * $Id$
 */
package org.jnode.test.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

        IFoo origFoo = new FooImpl();
        
        IFoo proxy = (IFoo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class[] { IFoo.class }, new Handler<IFoo>(origFoo));

        proxy.foo();
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
    }
    
    public static class FooImpl implements IFoo {

        /**
         * @see org.jnode.test.core.ProxyTest.IFoo#foo()
         */
        public void foo() {
            System.out.println("Foo");
        }
        
    }
    
    public static class Handler<T> implements InvocationHandler {

        private final T object;
        
        public Handler(T object) {
            this.object = object;
        }
        
        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {            
            return method.invoke(object, args);
        }        
    }
}
