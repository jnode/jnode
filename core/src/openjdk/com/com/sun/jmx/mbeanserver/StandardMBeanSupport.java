/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.jmx.mbeanserver;

import static com.sun.jmx.mbeanserver.Util.*;

import java.lang.reflect.Method;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Base class for Standard MBeans.
 *
 * @since 1.6
 */
public class StandardMBeanSupport extends MBeanSupport<Method> {

    /**
       <p>Construct a Standard MBean that wraps the given resource using the
       given Standard MBean interface.</p>

       @param resource the underlying resource for the new MBean.

       @param mbeanInterface the interface to be used to determine
       the MBean's management interface.

       @param <T> a type parameter that allows the compiler to check
       that {@code resource} implements {@code mbeanInterface},
       provided that {@code mbeanInterface} is a class constant like
       {@code SomeMBean.class}.

       @throws IllegalArgumentException if {@code resource} is null or
       if it does not implement the class {@code mbeanInterface} or if
       that class is not a valid Standard MBean interface.
    */
    public <T> StandardMBeanSupport(T resource, Class<T> mbeanInterface)
            throws NotCompliantMBeanException {
        super(resource, mbeanInterface);
    }

    @Override
    MBeanIntrospector<Method> getMBeanIntrospector() {
	return StandardMBeanIntrospector.getInstance();
    }

    @Override
    Object getCookie() {
	return null;
    }

    @Override
    public void register(MBeanServer mbs, ObjectName name) {}

    @Override
    public void unregister() {}

    /* Standard MBeans that are NotificationBroadcasters can return a different
     * MBeanNotificationInfo[] every time getMBeanInfo() is called, so we have
     * to reconstruct this MBeanInfo if necessary.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanInfo mbi = super.getMBeanInfo();
        Class<?> resourceClass = getResource().getClass();
        if (StandardMBeanIntrospector.isDefinitelyImmutableInfo(resourceClass))
            return mbi;
        return new MBeanInfo(mbi.getClassName(), mbi.getDescription(),
                mbi.getAttributes(), mbi.getConstructors(),
                mbi.getOperations(),
                MBeanIntrospector.findNotifications(getResource()),
                mbi.getDescriptor());
    }
}
