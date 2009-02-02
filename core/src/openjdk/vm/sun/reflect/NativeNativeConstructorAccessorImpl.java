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
 
package sun.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.VmReflection;

/**
 * @see sun.reflect.NativeConstructorAccessorImpl
 */
class NativeNativeConstructorAccessorImpl {
    /**
     * @see sun.reflect.NativeConstructorAccessorImpl#newInstance0(java.lang.reflect.Constructor, java.lang.Object[])
     */
    private static Object newInstance0(Constructor arg1, Object[] arg2) throws InstantiationException,
               IllegalArgumentException,
        InvocationTargetException{
        VmType vmt = VmType.fromClass(arg1.getDeclaringClass());
        VmMethod vmm = vmt.getDeclaredMethod(arg1.getSlot());
        try {
            return VmReflection.newInstance(vmm, arg2);
        } catch (IllegalAccessException iae)  { //todo| this should not happen, fix VmReflection.newInstance() to not
                                                //todo| throw this exception
            throw new InstantiationException("Unexpected IllegalAccessException");
        }
    }
}
