/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package sun.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.VmReflection;

/**
 * @see sun.reflect.NativeMethodAccessorImpl
 */
class NativeNativeMethodAccessorImpl {
    /**
     * @see sun.reflect.NativeMethodAccessorImpl#invoke0(java.lang.reflect.Method, java.lang.Object, java.lang.Object[])
     */
    private static Object invoke0(Method arg1, Object arg2, Object[] arg3) throws IllegalArgumentException,
        InvocationTargetException {
        VmType<?> vmt = VmType.fromClass((Class<?>) arg1.getDeclaringClass());
        VmMethod vmm = vmt.getDeclaredMethod(arg1.getSlot());
        return VmReflection.invoke(vmm, arg2, arg3);
    }
}
