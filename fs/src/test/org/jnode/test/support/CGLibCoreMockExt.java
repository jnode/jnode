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

package org.jnode.test.support;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.jmock.core.AbstractDynamicMock;
import org.jmock.core.DynamicMockError;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationDispatcher;

/**
 * @author Martin Kersten
 */
public class CGLibCoreMockExt extends AbstractDynamicMock {

    private Object proxy = null;

    public CGLibCoreMockExt(Class mockedClass, String name) {
        super(mockedClass, name);
    }

    public CGLibCoreMockExt(Class mockedClass, String name,
                            InvocationDispatcher invocationDispatcher) {
        super(mockedClass, name, invocationDispatcher);
    }

    private ClassLoader getClassLoader() {
        return getMockedType().getClassLoader();
    }

    protected Enhancer createEnhancer(Class mockedClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(getClassLoader());
        enhancer.setSuperclass(mockedClass);
        enhancer.setCallback(createMethodInterceptor());
        return enhancer;
    }

    protected MethodInterceptor createMethodInterceptor() {
        return new MyInterceptor();
    }

    public Object createProxy() {
        return createProxy(new Class[0], new Object[0]);
    }

    public Object createProxy(Class[] argumentTypes, Object[] arguments) {
        checkProxyCreationIsSupported();
        proxy = createEnhancer(getMockedType())
            .create(argumentTypes, arguments);
        return proxy;
    }

    private void checkProxyCreationIsSupported() {
        if (isProxyConstructed())
            throw new UnsupportedOperationException(
                "A proxy may only be created once.");
    }

    private boolean isProxyConstructed() {
        return proxy != null;
    }

    public Object proxy() {
        if (!isProxyConstructed())
            return createProxy();
        else
            return proxy();
    }

    private class MyInterceptor implements MethodInterceptor {
        public Object intercept(Object obj, Method method, Object[] args,
                                MethodProxy superProxy) throws Throwable {

            Invocation invocation = new Invocation(proxy, method, args);
            try {
                return mockInvocation(invocation);
            } catch (DynamicMockError e) {
                if (!isProxyConstructed())
                    return superProxy.invokeSuper(obj, args);
                else
                    throw e;
            }
        }
    }
}
